package tn.bewerbi.identity.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bewerbi.common.api.exception.ConflictException;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.common.events.DomainEvents;
import tn.bewerbi.common.events.EventPublisher;
import tn.bewerbi.common.events.Topics;
import tn.bewerbi.common.i18n.LocaleContext;
import tn.bewerbi.identity.domain.*;

@Service
@Transactional
public class AuthService {

    private static final SecureRandom RND = new SecureRandom();

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PasswordEncoder passwords;
    private final JwtTokenService tokens;
    private final EventPublisher events;
    private final RefreshTokenStore refreshStore;

    public AuthService(UserRepository users, ProfileRepository profiles,
                       PasswordEncoder passwords, JwtTokenService tokens,
                       EventPublisher events, RefreshTokenStore refreshStore) {
        this.users = users; this.profiles = profiles;
        this.passwords = passwords; this.tokens = tokens;
        this.events = events;
        this.refreshStore = refreshStore;
    }

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email().toLowerCase())) {
            throw new ConflictException("Email already registered", "error.auth.email.exists");
        }
        var user = new User(req.email().toLowerCase(), passwords.encode(req.password()), req.role());
        user.setPreferredLocale(LocaleContext.currentTag());
        user.setEmailVerification(randomToken(), Instant.now().plus(48, ChronoUnit.HOURS));
        users.save(user);

        var profile = new Profile(user.getId());
        profile.setFirstName(req.firstName());
        profile.setLastName(req.lastName());
        profile.setCountry("Tunesien");
        profiles.save(profile);

        events.publish(Topics.USER_REGISTERED, user.getId().toString(),
                new DomainEvents.UserRegistered(
                        user.getId(),
                        user.getEmail(),
                        req.firstName(),
                        user.getRole().name(),
                        user.getPreferredLocale(),
                        user.getEmailVerificationToken(),
                        Instant.now()));

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

    /**
     * Token rotation: the submitted refresh token is revoked on success, and
     * a new access + refresh pair is issued. A second call with the same
     * refresh token is rejected — protecting against replayed tokens.
     */
    public AuthResponse refresh(String refreshToken) {
        UUID userId = tokens.validateRefresh(refreshToken);
        if (!refreshStore.isKnown(userId, refreshToken)) {
            throw new BadCredentialsException("Refresh token revoked or reused");
        }
        refreshStore.revoke(userId, refreshToken);
        var user = users.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return issueTokens(user);
    }

    public void logout(UUID userId, String refreshToken) {
        refreshStore.revoke(userId, refreshToken);
    }

    /** Sign the user out of *every* device — useful after password reset. */
    public void logoutAll(UUID userId) {
        refreshStore.revokeAll(userId);
    }

    public void verifyEmail(String token) {
        var user = users.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid verification token"));
        if (user.getEmailVerificationExpiresAt() != null
                && user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Verification token expired");
        }
        user.markEmailVerified();
    }

    private AuthResponse issueTokens(User user) {
        var access = tokens.issueAccess(user);
        var refresh = tokens.issueRefresh(user.getId());
        refreshStore.remember(user.getId(), refresh.token(),
                Duration.ofSeconds(tokens.refreshTtlSeconds()));
        return new AuthResponse(
                access.token(),
                access.expiresAt(),
                tokens.accessTtlSeconds(),
                refresh.token(),
                refresh.expiresAt(),
                new AuthResponse.UserDto(user.getId(), user.getEmail(), user.getRole(),
                        user.isEmailVerified(), user.getPreferredLocale()));
    }

    private static String randomToken() {
        byte[] b = new byte[32];
        RND.nextBytes(b);
        return HexFormat.of().formatHex(b);
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
            /** TTL of the access token in seconds — lets the client schedule its
             * auto-refresh without parsing the JWT. */
            int accessTokenExpiresIn,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            UserDto user) {
        public record UserDto(UUID id, String email, UserRole role,
                              boolean emailVerified, String preferredLocale) {}
    }
}
