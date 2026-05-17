package tn.bewerbi.applications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.ConflictException;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.application.Application;
import tn.bewerbi.domain.application.ApplicationRepository;
import tn.bewerbi.domain.application.ApplicationStatus;
import tn.bewerbi.domain.job.JobRepository;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications")
@PreAuthorize("isAuthenticated()")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) { this.service = service; }

    @GetMapping("/mine")
    @Operation(summary = "List the current applicant's applications")
    public Page<ApplicationResponse> mine(Pageable pageable) {
        return service.mine(CurrentUser.id(), pageable);
    }

    @PostMapping
    @Operation(summary = "Apply to a job")
    public ApplicationResponse apply(@Valid @RequestBody ApplyRequest req) {
        return service.apply(CurrentUser.id(), req);
    }

    @PatchMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw an application")
    public ApplicationResponse withdraw(@PathVariable UUID id) {
        return service.withdraw(CurrentUser.id(), id);
    }

    public record ApplyRequest(@NotNull UUID jobId, String coverLetter) {}

    public record ApplicationResponse(
            UUID id, UUID jobId, UUID applicantUserId, String coverLetter,
            ApplicationStatus status, Integer matchScore, Instant createdAt) {}

    @Service
    @Transactional
    public static class ApplicationService {

        private final ApplicationRepository apps;
        private final JobRepository jobs;

        public ApplicationService(ApplicationRepository apps, JobRepository jobs) {
            this.apps = apps;
            this.jobs = jobs;
        }

        @Transactional(readOnly = true)
        public Page<ApplicationResponse> mine(UUID userId, Pageable pageable) {
            return apps.findByApplicantUserId(userId, pageable).map(this::toResponse);
        }

        public ApplicationResponse apply(UUID userId, ApplyRequest req) {
            var job = jobs.findById(req.jobId()).orElseThrow(() -> ResourceNotFoundException.of("Job", req.jobId()));
            apps.findByJobIdAndApplicantUserId(req.jobId(), userId).ifPresent(a -> {
                throw new ConflictException("You already applied to this job");
            });
            var application = new Application(req.jobId(), userId, req.coverLetter());
            apps.save(application);
            return toResponse(application);
        }

        public ApplicationResponse withdraw(UUID userId, UUID appId) {
            var a = apps.findById(appId).orElseThrow(() -> ResourceNotFoundException.of("Application", appId));
            if (!a.getApplicantUserId().equals(userId)) {
                throw new ConflictException("Not your application");
            }
            a.setStatus(ApplicationStatus.WITHDRAWN);
            return toResponse(a);
        }

        private ApplicationResponse toResponse(Application a) {
            return new ApplicationResponse(a.getId(), a.getJobId(), a.getApplicantUserId(),
                    a.getCoverLetter(), a.getStatus(), a.getMatchScore(), a.getCreatedAt());
        }
    }
}
