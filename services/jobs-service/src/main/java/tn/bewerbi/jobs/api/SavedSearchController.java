package tn.bewerbi.jobs.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.jobs.domain.GermanLevel;
import tn.bewerbi.jobs.domain.JobCategory;
import tn.bewerbi.jobs.domain.JobType;

@RestController
@RequestMapping("/api/v1/saved-searches")
@Tag(name = "Saved Searches")
@PreAuthorize("isAuthenticated()")
public class SavedSearchController {

    private final SavedSearchService service;
    public SavedSearchController(SavedSearchService service) { this.service = service; }

    @GetMapping
    public List<SavedSearchResponse> list() { return service.list(CurrentUser.id()); }

    @PostMapping
    public SavedSearchResponse create(@Valid @RequestBody SavedSearchRequest req) {
        return service.create(CurrentUser.id(), req);
    }

    @PutMapping("/{id}")
    public SavedSearchResponse update(@PathVariable UUID id, @Valid @RequestBody SavedSearchRequest req) {
        return service.update(CurrentUser.id(), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) { service.delete(CurrentUser.id(), id); }

    public enum AlertFrequency { INSTANT, DAILY, WEEKLY, OFF }

    public record SavedSearchRequest(
            @NotBlank String name, String query, JobCategory category, JobType type,
            String location, GermanLevel minGermanLevel, Integer salaryMin,
            boolean alertsEnabled, AlertFrequency alertFrequency) {}

    public record SavedSearchResponse(
            UUID id, String name, String query, JobCategory category, JobType type,
            String location, GermanLevel minGermanLevel, Integer salaryMin,
            boolean alertsEnabled, AlertFrequency alertFrequency, Instant createdAt) {}

    @Entity
    @Table(name = "saved_searches")
    @EntityListeners(AuditingEntityListener.class)
    public static class SavedSearch {
        @Id @Column(columnDefinition = "uuid") private UUID id = UUID.randomUUID();
        @Column(name = "user_id", nullable = false) private UUID userId;
        @Column(nullable = false) private String name;
        private String query;
        @Enumerated(EnumType.STRING) private JobCategory category;
        @Enumerated(EnumType.STRING) private JobType type;
        private String location;
        @Enumerated(EnumType.STRING) @Column(name = "min_german_level") private GermanLevel minGermanLevel;
        @Column(name = "salary_min") private Integer salaryMin;
        @Column(name = "alerts_enabled", nullable = false) private boolean alertsEnabled = true;
        @Enumerated(EnumType.STRING) @Column(name = "alert_frequency", nullable = false)
        private AlertFrequency alertFrequency = AlertFrequency.DAILY;
        @Column(name = "last_notified_at") private Instant lastNotifiedAt;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

        protected SavedSearch() {}
        public SavedSearch(UUID userId, String name) { this.userId = userId; this.name = name; }

        public UUID getId() { return id; }
        public UUID getUserId() { return userId; }
        public String getName() { return name; } public void setName(String v) { this.name = v; }
        public String getQuery() { return query; } public void setQuery(String v) { this.query = v; }
        public JobCategory getCategory() { return category; } public void setCategory(JobCategory v) { this.category = v; }
        public JobType getType() { return type; } public void setType(JobType v) { this.type = v; }
        public String getLocation() { return location; } public void setLocation(String v) { this.location = v; }
        public GermanLevel getMinGermanLevel() { return minGermanLevel; } public void setMinGermanLevel(GermanLevel v) { this.minGermanLevel = v; }
        public Integer getSalaryMin() { return salaryMin; } public void setSalaryMin(Integer v) { this.salaryMin = v; }
        public boolean isAlertsEnabled() { return alertsEnabled; } public void setAlertsEnabled(boolean v) { this.alertsEnabled = v; }
        public AlertFrequency getAlertFrequency() { return alertFrequency; } public void setAlertFrequency(AlertFrequency v) { this.alertFrequency = v; }
        public Instant getCreatedAt() { return createdAt; }
    }

    public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {
        List<SavedSearch> findByUserIdOrderByCreatedAtDesc(UUID userId);
    }

    @Service
    @Transactional
    public static class SavedSearchService {
        private final SavedSearchRepository repo;
        public SavedSearchService(SavedSearchRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public List<SavedSearchResponse> list(UUID userId) {
            return repo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::to).toList();
        }

        public SavedSearchResponse create(UUID userId, SavedSearchRequest r) {
            var s = new SavedSearch(userId, r.name());
            apply(s, r);
            return to(repo.save(s));
        }

        public SavedSearchResponse update(UUID userId, UUID id, SavedSearchRequest r) {
            var s = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SavedSearch", id));
            if (!s.getUserId().equals(userId)) throw ResourceNotFoundException.of("SavedSearch", id);
            s.setName(r.name());
            apply(s, r);
            return to(s);
        }

        public void delete(UUID userId, UUID id) {
            var s = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SavedSearch", id));
            if (!s.getUserId().equals(userId)) throw ResourceNotFoundException.of("SavedSearch", id);
            repo.delete(s);
        }

        private void apply(SavedSearch s, SavedSearchRequest r) {
            s.setQuery(r.query());
            s.setCategory(r.category());
            s.setType(r.type());
            s.setLocation(r.location());
            s.setMinGermanLevel(r.minGermanLevel());
            s.setSalaryMin(r.salaryMin());
            s.setAlertsEnabled(r.alertsEnabled());
            s.setAlertFrequency(r.alertFrequency() == null ? AlertFrequency.DAILY : r.alertFrequency());
        }

        private SavedSearchResponse to(SavedSearch s) {
            return new SavedSearchResponse(s.getId(), s.getName(), s.getQuery(), s.getCategory(),
                    s.getType(), s.getLocation(), s.getMinGermanLevel(), s.getSalaryMin(),
                    s.isAlertsEnabled(), s.getAlertFrequency(), s.getCreatedAt());
        }
    }
}
