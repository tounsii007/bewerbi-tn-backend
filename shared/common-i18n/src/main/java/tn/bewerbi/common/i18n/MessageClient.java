package tn.bewerbi.common.i18n;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client for i18n-service.  Used by any backend service that needs to resolve
 * a message key (for outbound emails, notifications or server-side rendering).
 *
 * Caches per-locale bundles in Redis for 5 minutes to avoid hot-pathing the
 * i18n-service from every request.
 */
@Component
public class MessageClient {

    private final RestClient restClient;
    private final StringRedisTemplate redis;

    public MessageClient(
            @Value("${bewerbi.clients.i18n-service.base-url:http://localhost:8181}") String baseUrl,
            StringRedisTemplate redis) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.redis = redis;
    }

    /**
     * Resolve the given message key in the locale currently bound to
     * {@link LocaleContext}. Falls back to the key itself if unavailable.
     */
    @CircuitBreaker(name = "i18n-service", fallbackMethod = "fallbackResolve")
    @Retry(name = "i18n-service")
    public String resolve(String key, Object... args) {
        return resolveIn(LocaleContext.currentTag(), key, args);
    }

    public String resolveIn(String locale, String key, Object... args) {
        String template = bundleFor(locale).getOrDefault(key, key);
        return format(template, args);
    }

    private Map<String, String> bundleFor(String locale) {
        String cacheKey = "i18n:bundle:" + locale;
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                return parseBundle(cached);
            }
        } catch (Exception ignored) {}

        @SuppressWarnings("unchecked")
        Map<String, String> fresh = restClient.get()
                .uri("/api/v1/i18n/messages?locale={l}", locale)
                .retrieve()
                .body(Map.class);
        if (fresh == null) return Map.of();

        try {
            redis.opsForValue().set(cacheKey, serializeBundle(fresh),
                    java.time.Duration.ofMinutes(5));
        } catch (Exception ignored) {}
        return fresh;
    }

    @SuppressWarnings("unused")
    private String fallbackResolve(String key, Object[] args, Throwable ex) {
        return format(key, args);
    }

    private static String format(String template, Object[] args) {
        if (args == null || args.length == 0) return template;
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }

    private static String serializeBundle(Map<String, String> bundle) {
        StringBuilder sb = new StringBuilder();
        bundle.forEach((k, v) -> sb.append(k).append('\u0001').append(v).append('\u0002'));
        return sb.toString();
    }

    private static Map<String, String> parseBundle(String raw) {
        var out = new java.util.HashMap<String, String>();
        for (String entry : raw.split("\u0002")) {
            if (entry.isEmpty()) continue;
            int sep = entry.indexOf('\u0001');
            if (sep > 0) out.put(entry.substring(0, sep), entry.substring(sep + 1));
        }
        return out;
    }
}
