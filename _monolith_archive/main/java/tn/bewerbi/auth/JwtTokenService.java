package tn.bewerbi.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import tn.bewerbi.config.BewerbiProperties;
import tn.bewerbi.domain.user.User;
import tn.bewerbi.domain.user.UserRole;

@Service
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final BewerbiProperties properties;

    public JwtTokenService(JwtEncoder encoder, JwtDecoder decoder, BewerbiProperties properties) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.properties = properties;
    }

    public AccessToken issueAccess(User user) {
        var now = Instant.now();
        var expires = now.plus(Duration.ofMinutes(properties.security().jwt().accessTtlMinutes()));
        var claims = JwtClaimsSet.builder()
                .issuer(properties.security().jwt().issuer())
                .issuedAt(now)
                .expiresAt(expires)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .claim("token_type", "access")
                .build();
        var headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
        return new AccessToken(token, expires);
    }

    public RefreshToken issueRefresh(UUID userId) {
        var now = Instant.now();
        var expires = now.plus(Duration.ofDays(properties.security().jwt().refreshTtlDays()));
        var claims = JwtClaimsSet.builder()
                .issuer(properties.security().jwt().issuer())
                .issuedAt(now)
                .expiresAt(expires)
                .subject(userId.toString())
                .claim("token_type", "refresh")
                .build();
        var headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
        return new RefreshToken(token, expires);
    }

    public UUID validateRefresh(String token) {
        var decoded = decoder.decode(token);
        if (!"refresh".equals(decoded.getClaimAsString("token_type"))) {
            throw new IllegalArgumentException("Not a refresh token");
        }
        return UUID.fromString(decoded.getSubject());
    }

    public record AccessToken(String token, Instant expiresAt) {}
    public record RefreshToken(String token, Instant expiresAt) {}
}
