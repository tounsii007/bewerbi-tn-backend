package tn.bewerbi.jobs;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.bewerbi.api.BadRequestException;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.company.Company;
import tn.bewerbi.domain.company.CompanyRepository;
import tn.bewerbi.domain.company.VerificationStatus;
import tn.bewerbi.domain.job.*;
import tn.bewerbi.domain.profile.GermanLevel;

@Service
@Transactional
public class JobService {

    private final JobRepository jobs;
    private final CompanyRepository companies;

    public JobService(JobRepository jobs, CompanyRepository companies) {
        this.jobs = jobs;
        this.companies = companies;
    }

    @Transactional(readOnly = true)
    public Page<JobDtos.JobResponse> search(
            String search, JobCategory category, JobType type, String location,
            GermanLevel minGermanLevel, Integer salaryMin, Pageable pageable) {
        var spec = JobSpecifications.withFilters(search, category, type, location, minGermanLevel, salaryMin);
        return jobs.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobDtos.JobResponse get(UUID id) {
        var job = jobs.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Job", id));
        return toResponse(job);
    }

    public JobDtos.JobResponse create(UUID employerUserId, JobDtos.JobCreateRequest req) {
        var company = companies.findById(req.companyId())
                .orElseThrow(() -> ResourceNotFoundException.of("Company", req.companyId()));
        if (!company.getOwnerUserId().equals(employerUserId)) {
            throw new BadRequestException("You do not own this company");
        }
        validateSalary(req.salaryMin(), req.salaryMax());

        var job = new Job(req.companyId(), employerUserId, req.title(), req.description(),
                req.category(), req.type(), req.location());
        job.setRequirements(req.requirements());
        job.setSalaryMin(req.salaryMin());
        job.setSalaryMax(req.salaryMax());
        if (req.salaryCurrency() != null) job.setSalaryCurrency(req.salaryCurrency());
        job.setGermanLevel(req.germanLevel());
        jobs.save(job);
        return toResponse(job);
    }

    public JobDtos.JobResponse update(UUID employerUserId, UUID jobId, JobDtos.JobUpdateRequest req) {
        var job = jobs.findById(jobId).orElseThrow(() -> ResourceNotFoundException.of("Job", jobId));
        if (!job.getEmployerUserId().equals(employerUserId)) {
            throw new BadRequestException("Not your job");
        }
        if (req.title() != null) job.setTitle(req.title());
        if (req.description() != null) job.setDescription(req.description());
        if (req.requirements() != null) job.setRequirements(req.requirements());
        if (req.category() != null) job.setCategory(req.category());
        if (req.type() != null) job.setType(req.type());
        if (req.location() != null) job.setLocation(req.location());
        if (req.salaryMin() != null) job.setSalaryMin(req.salaryMin());
        if (req.salaryMax() != null) job.setSalaryMax(req.salaryMax());
        if (req.germanLevel() != null) job.setGermanLevel(req.germanLevel());
        if (req.status() != null) job.setStatus(req.status());
        validateSalary(job.getSalaryMin(), job.getSalaryMax());
        return toResponse(job);
    }

    public void delete(UUID employerUserId, UUID jobId) {
        var job = jobs.findById(jobId).orElseThrow(() -> ResourceNotFoundException.of("Job", jobId));
        if (!job.getEmployerUserId().equals(employerUserId)) {
            throw new BadRequestException("Not your job");
        }
        jobs.delete(job);
    }

    private void validateSalary(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw new BadRequestException("salaryMin cannot exceed salaryMax");
        }
    }

    private JobDtos.JobResponse toResponse(Job job) {
        Company company = companies.findById(job.getCompanyId()).orElse(null);
        String companyName = company != null ? company.getName() : "";
        boolean verified = company != null && company.getVerificationStatus() == VerificationStatus.VERIFIED;
        return new JobDtos.JobResponse(
                job.getId(),
                job.getCompanyId(),
                companyName,
                job.getTitle(),
                job.getDescription(),
                job.getRequirements(),
                job.getCategory(),
                job.getType(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.getGermanLevel(),
                job.getStatus(),
                job.isPremium(),
                verified,
                job.getCreatedAt());
    }
}
