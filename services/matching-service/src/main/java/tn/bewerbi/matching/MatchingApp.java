package tn.bewerbi.matching;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.security.JwtSecurityConfig;
import tn.bewerbi.common.security.SecurityFilterChainRegistrar;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class, SecurityFilterChainRegistrar.class})
public class MatchingApp {
    public static void main(String[] args) { SpringApplication.run(MatchingApp.class, args); }

    @Bean public RestClient restClient() { return RestClient.builder().build(); }

    public record JobSummary(java.util.UUID id, String title, String description,
                              String requirements, String location,
                              Integer salaryMin, Integer salaryMax,
                              String germanLevel, boolean premium) {}
    public record Profile(String desiredProfession, String germanLevel, String city,
                          List<String> skills) {}
    public record Recommendation(JobSummary job, int matchPercent, List<String> reasons) {}

    @RestController @RequestMapping("/api/v1/matching") @PreAuthorize("isAuthenticated()")
    public static class MatchingController {
        private final MatchingService svc;
        public MatchingController(MatchingService svc) { this.svc = svc; }

        @GetMapping("/recommendations")
        public List<Recommendation> recommendations(@RequestParam(defaultValue = "10") int limit) {
            return svc.recommend(CurrentUser.id(), Math.max(1, Math.min(limit, 50)));
        }
    }

    @Service
    public static class MatchingService {
        private final RestClient http;
        @Value("${bewerbi.clients.jobs-service.base-url}") private String jobsUrl;
        @Value("${bewerbi.clients.identity-service.base-url}") private String idUrl;

        public MatchingService(RestClient http) { this.http = http; }

        @CircuitBreaker(name = "default", fallbackMethod = "empty")
        public List<Recommendation> recommend(java.util.UUID userId, int limit) {
            String token = currentToken();

            var profile = http.get().uri(idUrl + "/api/v1/profile/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve().body(Map.class);
            if (profile == null) return List.of();

            @SuppressWarnings("unchecked")
            var jobsPage = http.get().uri(jobsUrl + "/api/v1/jobs?size=200")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve().body(Map.class);
            if (jobsPage == null) return List.of();
            @SuppressWarnings("unchecked")
            var jobs = (List<Map<String,Object>>) jobsPage.get("content");

            Profile p = toProfile(profile);
            return jobs.stream().map(j -> score(j, p))
                    .filter(r -> r.matchPercent() > 0)
                    .sorted(Comparator.comparingInt(Recommendation::matchPercent).reversed())
                    .limit(limit).toList();
        }

        @SuppressWarnings("unused")
        private List<Recommendation> empty(java.util.UUID userId, int limit, Throwable t) {
            return List.of();
        }

        private static String currentToken() {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) return jwt.getTokenValue();
            return "";
        }

        @SuppressWarnings("unchecked")
        private static Profile toProfile(Map<String,Object> raw) {
            return new Profile(
                    (String) raw.get("desiredProfession"),
                    (String) raw.get("germanLevel"),
                    (String) raw.get("city"),
                    (List<String>) raw.getOrDefault("skills", List.of()));
        }

        private static Recommendation score(Map<String,Object> job, Profile p) {
            int score = 0;
            var reasons = new ArrayList<String>();
            String title = (String) job.getOrDefault("title", "");
            String desc = (String) job.getOrDefault("description", "");
            String req = (String) job.getOrDefault("requirements", "");
            String location = (String) job.getOrDefault("location", "");
            String gl = (String) job.get("germanLevel");
            boolean premium = Boolean.TRUE.equals(job.get("premium"));

            String blob = (title + " " + desc + " " + (req == null ? "" : req)).toLowerCase();

            if (p.desiredProfession() != null && !p.desiredProfession().isBlank()) {
                String want = p.desiredProfession().toLowerCase();
                if (blob.contains(want)) { score += 35; reasons.add("Passt zum gewünschten Beruf"); }
            }
            if (p.germanLevel() != null && gl != null) {
                if (p.germanLevel().compareTo(gl) >= 0) { score += 25; reasons.add("Deutsch ausreichend"); }
                else score = Math.max(0, score - 15);
            }
            if (p.city() != null && !p.city().isBlank() && location != null
                    && location.toLowerCase().contains(p.city().toLowerCase())) {
                score += 15; reasons.add("Standort passt");
            }
            if (p.skills() != null) {
                long hits = p.skills().stream().map(String::toLowerCase).filter(blob::contains).count();
                if (hits > 0) { score += (int) Math.min(hits * 10, 25); reasons.add(hits + " Skills passen"); }
            }
            if (premium) score += 5;

            var summary = new JobSummary(
                    java.util.UUID.fromString((String) job.get("id")), title, desc, req, location,
                    (Integer) job.get("salaryMin"), (Integer) job.get("salaryMax"), gl, premium);
            return new Recommendation(summary, Math.min(100, score), reasons);
        }
    }
}
