package tn.bewerbi.search;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.job.JobCategory;
import tn.bewerbi.domain.job.JobType;
import tn.bewerbi.domain.profile.GermanLevel;
import tn.bewerbi.domain.search.SavedSearch;
import tn.bewerbi.domain.search.SavedSearchRepository;

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

    public record SavedSearchRequest(
            @NotBlank String name, String query, JobCategory category, JobType type,
            String location, GermanLevel minGermanLevel, Integer salaryMin, boolean alertsEnabled) {}

    public record SavedSearchResponse(
            UUID id, String name, String query, JobCategory category, JobType type,
            String location, GermanLevel minGermanLevel, Integer salaryMin, boolean alertsEnabled) {}

    @Service
    @Transactional
    public static class SavedSearchService {

        private final SavedSearchRepository repo;

        public SavedSearchService(SavedSearchRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public List<SavedSearchResponse> list(UUID userId) {
            return repo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
        }

        public SavedSearchResponse create(UUID userId, SavedSearchRequest req) {
            var s = new SavedSearch(userId, req.name());
            apply(s, req);
            return toResponse(repo.save(s));
        }

        public SavedSearchResponse update(UUID userId, UUID id, SavedSearchRequest req) {
            var s = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SavedSearch", id));
            if (!s.getUserId().equals(userId)) throw ResourceNotFoundException.of("SavedSearch", id);
            s.setName(req.name());
            apply(s, req);
            return toResponse(s);
        }

        public void delete(UUID userId, UUID id) {
            var s = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("SavedSearch", id));
            if (!s.getUserId().equals(userId)) throw ResourceNotFoundException.of("SavedSearch", id);
            repo.delete(s);
        }

        private void apply(SavedSearch s, SavedSearchRequest req) {
            s.setQuery(req.query());
            s.setCategory(req.category());
            s.setType(req.type());
            s.setLocation(req.location());
            s.setMinGermanLevel(req.minGermanLevel());
            s.setSalaryMin(req.salaryMin());
            s.setAlertsEnabled(req.alertsEnabled());
        }

        private SavedSearchResponse toResponse(SavedSearch s) {
            return new SavedSearchResponse(
                    s.getId(), s.getName(), s.getQuery(), s.getCategory(), s.getType(),
                    s.getLocation(), s.getMinGermanLevel(), s.getSalaryMin(), s.isAlertsEnabled());
        }
    }
}
