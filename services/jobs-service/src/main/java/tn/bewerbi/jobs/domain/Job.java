package tn.bewerbi.jobs.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "jobs")
@EntityListeners(AuditingEntityListener.class)
public class Job {

    @Id @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "employer_user_id", nullable = false) private UUID employerUserId;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Column(columnDefinition = "text") private String requirements;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobCategory category;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobType type;
    @Column(nullable = false, length = 120) private String location;
    @Column(name = "salary_min") private Integer salaryMin;
    @Column(name = "salary_max") private Integer salaryMax;
    @Column(name = "salary_currency", length = 4) private String salaryCurrency = "EUR";
    @Enumerated(EnumType.STRING) @Column(name = "german_level") private GermanLevel germanLevel;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobStatus status = JobStatus.ACTIVE;
    @Column(nullable = false) private boolean premium;

    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Job() {}

    public Job(UUID companyId, UUID employerUserId, String title, String description,
               JobCategory category, JobType type, String location) {
        this.companyId = companyId; this.employerUserId = employerUserId;
        this.title = title; this.description = description;
        this.category = category; this.type = type; this.location = location;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public UUID getEmployerUserId() { return employerUserId; }
    public String getTitle() { return title; } public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; } public void setDescription(String v) { this.description = v; }
    public String getRequirements() { return requirements; } public void setRequirements(String v) { this.requirements = v; }
    public JobCategory getCategory() { return category; } public void setCategory(JobCategory v) { this.category = v; }
    public JobType getType() { return type; } public void setType(JobType v) { this.type = v; }
    public String getLocation() { return location; } public void setLocation(String v) { this.location = v; }
    public Integer getSalaryMin() { return salaryMin; } public void setSalaryMin(Integer v) { this.salaryMin = v; }
    public Integer getSalaryMax() { return salaryMax; } public void setSalaryMax(Integer v) { this.salaryMax = v; }
    public String getSalaryCurrency() { return salaryCurrency; } public void setSalaryCurrency(String v) { this.salaryCurrency = v; }
    public GermanLevel getGermanLevel() { return germanLevel; } public void setGermanLevel(GermanLevel v) { this.germanLevel = v; }
    public JobStatus getStatus() { return status; } public void setStatus(JobStatus v) { this.status = v; }
    public boolean isPremium() { return premium; } public void setPremium(boolean v) { this.premium = v; }
    public Instant getCreatedAt() { return createdAt; }
}
