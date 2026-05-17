package tn.bewerbi.matching;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.job.Job;
import tn.bewerbi.domain.job.JobRepository;
import tn.bewerbi.domain.job.JobStatus;
import tn.bewerbi.domain.profile.Profile;
import tn.bewerbi.domain.profile.ProfileRepository;
import tn.bewerbi.jobs.JobDtos;

/**
 * Recommends jobs based on the current user's profile using a simple scoring heuristic.
 * Later evolvable to embedding-based matching without changing the API.
 */
@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching")
@PreAuthorize("isAuthenticated()")
public class MatchingController {

    private final MatchingService service;

    public MatchingController(MatchingService service) { this.service = service; }

    @GetMapping("/recommendations")
    @Operation(summary = "Top-N job recommendations with a match percentage")
    public List<Recommendation> recommendations(@RequestParam(defaultValue = "10") int limit) {
        return service.recommend(CurrentUser.id(), Math.min(Math.max(limit, 1), 50));
    }

    public record Recommendation(JobDtos.JobResponse job, int matchPercent, List<String> reasons) {}

    @Service
    @Transactional(readOnly = true)
    public static class MatchingService {

        private final JobRepository jobs;
        private final ProfileRepository profiles;

        public MatchingService(JobRepository jobs, ProfileRepository profiles) {
            this.jobs = jobs;
            this.profiles = profiles;
        }

        public List<Recommendation> recommend(java.util.UUID userId, int limit) {
            Profile p = profiles.findByUserId(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Profile", userId));
            var activeJobs = jobs.findByStatus(JobStatus.ACTIVE, PageRequest.of(0, 500)).getContent();

            var scored = activeJobs.stream()
                    .map(job -> score(job, p))
                    .filter(r -> r.matchPercent() > 0)
                    .sorted(Comparator.comparingInt(Recommendation::matchPercent).reversed())
                    .limit(limit)
                    .toList();
            return scored;
        }

        private Recommendation score(Job job, Profile p) {
            int score = 0;
            var reasons = new ArrayList<String>();

            // Desired profession
            if (p.getDesiredProfession() != null && !p.getDesiredProfession().isBlank()) {
                String want = p.getDesiredProfession().toLowerCase();
                if (job.getTitle().toLowerCase().contains(want)
                        || job.getDescription().toLowerCase().contains(want)) {
                    score += 35;
                    reasons.add("Passt zum gewünschten Beruf");
                }
            }

            // German level
            if (p.getGermanLevel() != null && job.getGermanLevel() != null) {
                if (p.getGermanLevel().meetsOrExceeds(job.getGermanLevel())) {
                    score += 25;
                    reasons.add("Deutschkenntnisse ausreichend");
                } else {
                    score = Math.max(0, score - 15);
                }
            }

            // City match
            if (p.getCity() != null && !p.getCity().isBlank()
                    && job.getLocation().toLowerCase().contains(p.getCity().toLowerCase())) {
                score += 15;
                reasons.add("Standort passt");
            }

            // Skills overlap
            if (p.getSkills() != null && !p.getSkills().isEmpty()) {
                String blob = (job.getTitle() + " " + job.getDescription() + " "
                        + (job.getRequirements() == null ? "" : job.getRequirements())).toLowerCase();
                long hits = p.getSkills().stream()
                        .map(String::toLowerCase)
                        .filter(blob::contains)
                        .count();
                if (hits > 0) {
                    int delta = (int) Math.min(hits * 10, 25);
                    score += delta;
                    reasons.add(hits + " passende Skills");
                }
            }

            if (job.isPremium()) score += 5;

            int percent = Math.min(100, score);
            var dto = new JobDtos.JobResponse(
                    job.getId(), job.getCompanyId(), "", job.getTitle(), job.getDescription(),
                    job.getRequirements(), job.getCategory(), job.getType(), job.getLocation(),
                    job.getSalaryMin(), job.getSalaryMax(), job.getSalaryCurrency(),
                    job.getGermanLevel(), job.getStatus(), job.isPremium(), false, job.getCreatedAt());
            return new Recommendation(dto, percent, reasons);
        }
    }
}
