package tn.bewerbi.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Default filter chain — services that want extra public routes can declare
 * their own @Bean SecurityFilterChain which will take precedence thanks to
 * @ConditionalOnMissingBean.
 */
@Configuration
public class SecurityFilterChainRegistrar {

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthConverter,
            UrlBasedCorsConfigurationSource corsSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }
}
