package tn.bewerbi.jobs.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.exception.BadRequestException;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.jobs.domain.*;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs")
public class JobController {

    private final JobService service;
    public JobController(JobService service) { this.service = service; }

    @GetMapping
    public Page<JobResponse> search(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) JobCategory category,
                                    @RequestParam(required = false) JobType type,
                                    @RequestParam(required = false) String location,
                                    @RequestParam(required = false) GermanLevel minGermanLevel,
                                    @RequestParam(required = false) Integer salaryMin,
                                    Pageable pageable) {
        return service.search(search, category, type, location, minGermanLevel, salaryMin, pageable);
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable UUID id) { return service.get(id); }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public JobResponse create(@Valid @RequestBody JobCreateRequest req) {
        return service.create(CurrentUser.id(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public JobResponse update(@PathVariable UUID id, @RequestBody JobUpdateRequest req) {
        return service.update(CurrentUser.id(), id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public void delete(@PathVariable UUID id) { service.delete(CurrentUser.id(), id); }

    public record JobResponse(
            UUID id, UUID companyId, String title, String description, String requirements,
            JobCategory category, JobType type, String location,
            Integer salaryMin, Integer salaryMax, String salaryCurrency,
            GermanLevel germanLevel, JobStatus status, boolean premium, Instant createdAt) {}

    public record JobCreateRequest(
            @NotNull UUID companyId,
            @NotBlank @Size(max = 200) String title,
            @NotBlank String description,
            String requirements,
            @NotNull JobCategory category,
            @NotNull JobType type,
            @NotBlank @Size(max = 120) String location,
            @PositiveOrZero Integer salaryMin,
            @PositiveOrZero Integer salaryMax,
            String salaryCurrency,
            GermanLevel germanLevel) {}

    public record JobUpdateRequest(String title, String description, String requirements,
                                   JobCategory category, JobType type, String location,
                                   Integer salaryMin, Integer salaryMax,
                                   GermanLevel germanLevel, JobStatus status) {}

    @Service
    @Transactional
    public static class JobService {
        private final JobRepository repo;
        public JobService(JobRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public Page<JobResponse> search(String q, JobCategory c, JobType t, String loc,
                                        GermanLevel min, Integer sal, Pageable p) {
            return repo.findAll(JobSpecifications.filters(q, c, t, loc, min, sal), p).map(this::to);
        }

        @Transactional(readOnly = true)
        public JobResponse get(UUID id) {
            return repo.findById(id).map(this::to)
                    .orElseThrow(() -> ResourceNotFoundException.of("Job", id));
        }

        public JobResponse create(UUID userId, JobCreateRequest r) {
            validate(r.salaryMin(), r.salaryMax());
            var job = new Job(r.companyId(), userId, r.title(), r.description(),
                    r.category(), r.type(), r.location());
            job.setRequirements(r.requirements());
            job.setSalaryMin(r.salaryMin());
            job.setSalaryMax(r.salaryMax());
            if (r.salaryCurrency() != null) job.setSalaryCurrency(r.salaryCurrency());
            job.setGermanLevel(r.germanLevel());
            return to(repo.save(job));
        }

        public JobResponse update(UUID userId, UUID id, JobUpdateRequest r) {
            var job = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Job", id));
            if (!job.getEmployerUserId().equals(userId)) {
                throw new BadRequestException("Not your job", "error.jobs.notOwner");
            }
            if (r.title() != null) job.setTitle(r.title());
            if (r.description() != null) job.setDescription(r.description());
            if (r.requirements() != null) job.setRequirements(r.requirements());
            if (r.category() != null) job.setCategory(r.category());
            if (r.type() != null) job.setType(r.type());
            if (r.location() != null) job.setLocation(r.location());
            if (r.salaryMin() != null) job.setSalaryMin(r.salaryMin());
            if (r.salaryMax() != null) job.setSalaryMax(r.salaryMax());
            if (r.germanLevel() != null) job.setGermanLevel(r.germanLevel());
            if (r.status() != null) job.setStatus(r.status());
            validate(job.getSalaryMin(), job.getSalaryMax());
            return to(job);
        }

        public void delete(UUID userId, UUID id) {
            var job = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Job", id));
            if (!job.getEmployerUserId().equals(userId)) {
                throw new BadRequestException("Not your job", "error.jobs.notOwner");
            }
            repo.delete(job);
        }

        private void validate(Integer min, Integer max) {
            if (min != null && max != null && min > max) {
                throw new BadRequestException("salaryMin > salaryMax", "error.jobs.salaryRange");
            }
        }

        private JobResponse to(Job j) {
            return new JobResponse(j.getId(), j.getCompanyId(), j.getTitle(), j.getDescription(),
                    j.getRequirements(), j.getCategory(), j.getType(), j.getLocation(),
                    j.getSalaryMin(), j.getSalaryMax(), j.getSalaryCurrency(),
                    j.getGermanLevel(), j.getStatus(), j.isPremium(), j.getCreatedAt());
        }
    }
}
