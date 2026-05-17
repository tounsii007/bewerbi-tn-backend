package tn.bewerbi.jobs;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;
import tn.bewerbi.domain.job.JobCategory;
import tn.bewerbi.domain.job.JobStatus;
import tn.bewerbi.domain.job.JobType;
import tn.bewerbi.domain.profile.GermanLevel;

public final class JobDtos {

    private JobDtos() {}

    public record JobResponse(
            UUID id,
            UUID companyId,
            String companyName,
            String title,
            String description,
            String requirements,
            JobCategory category,
            JobType type,
            String location,
            Integer salaryMin,
            Integer salaryMax,
            String salaryCurrency,
            GermanLevel germanLevel,
            JobStatus status,
            boolean premium,
            boolean employerVerified,
            Instant createdAt) {}

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

    public record JobUpdateRequest(
            String title,
            String description,
            String requirements,
            JobCategory category,
            JobType type,
            String location,
            Integer salaryMin,
            Integer salaryMax,
            GermanLevel germanLevel,
            JobStatus status) {}
}
