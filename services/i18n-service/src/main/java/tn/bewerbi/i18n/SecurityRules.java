package tn.bewerbi.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Explicit security rules for i18n-service.
 *
 * - Read-only i18n lookups (messages, reference-data, professions) are public
 *   so unauthenticated pages (login, register, public job listings) can still
 *   be fully localized.
 * - {@code /api/v1/admin/**} requires an authenticated user with ROLE_ADMIN.
 * - Everything else is denied — default-deny ensures we can't accidentally
 *   expose a new endpoint by forgetting a rule.
 */
@Configuration
public class SecurityRules {

    @Bean
    public SecurityFilterChain i18nFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthConverter,
            UrlBasedCorsConfigurationSource corsSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Infra
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Public reads
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/i18n/messages",
                                "/api/v1/professions",
                                "/api/v1/reference-data/**").permitAll()
                        // Admin-only writes
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Default deny
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }
}
