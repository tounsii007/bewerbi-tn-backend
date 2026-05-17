package tn.bewerbi.gateway;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway-level auth: public routes for auth/register + job browsing + i18n
 * resources; everything else requires a valid JWT. Downstream services
 * re-verify so no implicit trust.
 */
@Configuration
public class SecurityConfig {

    @Value("${bewerbi.security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        var key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/api/v1/auth/verify-email",
                                "/api/v1/jobs/**",
                                "/api/v1/companies/**",
                                "/api/v1/i18n/**").permitAll()
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));
        return http.build();
    }
}
