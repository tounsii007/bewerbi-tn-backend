package tn.bewerbi.jobs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.domain.job.JobCategory;
import tn.bewerbi.domain.job.JobType;
import tn.bewerbi.domain.profile.GermanLevel;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Search / list active jobs with optional filters")
    public Page<JobDtos.JobResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) JobCategory category,
            @RequestParam(required = false) JobType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) GermanLevel minGermanLevel,
            @RequestParam(required = false) Integer salaryMin,
            Pageable pageable) {
        return jobService.search(search, category, type, location, minGermanLevel, salaryMin, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by id")
    public JobDtos.JobResponse get(@PathVariable UUID id) {
        return jobService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create a job listing (employer only)")
    public JobDtos.JobResponse create(@Valid @RequestBody JobDtos.JobCreateRequest req) {
        return jobService.create(CurrentUser.id(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public JobDtos.JobResponse update(@PathVariable UUID id, @RequestBody JobDtos.JobUpdateRequest req) {
        return jobService.update(CurrentUser.id(), id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public void delete(@PathVariable UUID id) {
        jobService.delete(CurrentUser.id(), id);
    }
}
