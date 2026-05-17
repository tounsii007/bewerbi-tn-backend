package tn.bewerbi.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bewerbi.api.ConflictException;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.profile.Profile;
import tn.bewerbi.domain.profile.ProfileRepository;
import tn.bewerbi.domain.user.User;
import tn.bewerbi.domain.user.UserRepository;
import tn.bewerbi.domain.user.UserRole;

@Service
@Transactional
public class AuthService {

    private static final SecureRandom RND = new SecureRandom();

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwords;
    private final JwtTokenService tokens;

    public AuthService(UserRepository users, ProfileRepository profiles,
                       PasswordEncoder passwords, JwtTokenService tokens) {
        this.users = users;
        this.profiles = profiles;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email().toLowerCase())) {
            throw new ConflictException("Email is already registered");
        }
        var user = new User(req.email().toLowerCase(), passwords.encode(req.password()), req.role());
        user.setEmailVerification(randomToken(), Instant.now().plus(48, ChronoUnit.HOURS));
        users.save(user);

        var profile = new Profile(user.getId());
        profile.setFirstName(req.firstName());
        profile.setLastName(req.lastName());
        profile.setCountry("Tunesien");
        profiles.save(profile);

        // TODO: send verification email via MailService (non-blocking)

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest req) {
        var user = users.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwords.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        user.touchLogin();
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        UUID userId = tokens.validateRefresh(refreshToken);
        var user = users.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        if (!user.getRefreshTokenHashes().contains(hash(refreshToken))) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }
        user.getRefreshTokenHashes().remove(hash(refreshToken));
        return issueTokens(user);
    }

    public void logout(UUID userId, String refreshToken) {
        users.findById(userId).ifPresent(u -> u.getRefreshTokenHashes().remove(hash(refreshToken)));
    }

    public void verifyEmail(String token) {
        var user = users.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired verification token"));
        if (user.getEmailVerificationExpiresAt() != null
                && user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Verification token expired");
        }
        user.markEmailVerified();
    }

    private AuthResponse issueTokens(User user) {
        var access = tokens.issueAccess(user);
        var refresh = tokens.issueRefresh(user.getId());
        user.getRefreshTokenHashes().add(hash(refresh.token()));
        return new AuthResponse(
                access.token(),
                access.expiresAt(),
                refresh.token(),
                refresh.expiresAt(),
                new AuthResponse.UserDto(user.getId(), user.getEmail(), user.getRole(), user.isEmailVerified()));
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        RND.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes()));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public record RegisterRequest(
            @jakarta.validation.constraints.Email String email,
            @jakarta.validation.constraints.Size(min = 8, max = 72) String password,
            @jakarta.validation.constraints.NotBlank String firstName,
            @jakarta.validation.constraints.NotBlank String lastName,
            UserRole role) {}

    public record LoginRequest(
            @jakarta.validation.constraints.Email String email,
            @jakarta.validation.constraints.NotBlank String password) {}

    public record AuthResponse(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            UserDto user) {

        public record UserDto(UUID id, String email, UserRole role, boolean emailVerified) {}
    }
}
