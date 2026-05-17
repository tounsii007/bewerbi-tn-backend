package tn.bewerbi.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

/**
 * Rate-limiting key resolvers.
 *
 * {@code userKey}: use the JWT subject (stable per user) for authenticated
 * requests. Falls back to the client IP so unauthenticated traffic is still
 * capped. Together with the {@code RequestRateLimiter} GatewayFilter (configured
 * in {@code application.yml}) this protects against credential stuffing and
 * runaway automation on public endpoints.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication() == null ? null : ctx.getAuthentication().getName())
                .defaultIfEmpty("")
                .map(sub -> {
                    if (sub != null && !sub.isBlank()) return "user:" + sub;
                    var addr = exchange.getRequest().getRemoteAddress();
                    return "ip:" + (addr == null ? "unknown" : addr.getAddress().getHostAddress());
                })
                .switchIfEmpty(Mono.just("ip:unknown"));
    }
}
