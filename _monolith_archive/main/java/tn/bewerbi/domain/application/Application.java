package tn.bewerbi.domain.application;

import jakarta.persistence.*;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "applications", indexes = {
        @Index(name = "idx_apps_applicant", columnList = "applicant_user_id"),
        @Index(name = "idx_apps_job", columnList = "job_id"),
        @Index(name = "uq_apps_unique", columnList = "job_id, applicant_user_id", unique = true)
})
public class Application extends BaseEntity {

    @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
    private UUID jobId;

    @Column(name = "applicant_user_id", nullable = false, columnDefinition = "uuid")
    private UUID applicantUserId;

    @Column(columnDefinition = "text")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "match_score")
    private Integer matchScore;

    protected Application() {}

    public Application(UUID jobId, UUID applicantUserId, String coverLetter) {
        this.jobId = jobId;
        this.applicantUserId = applicantUserId;
        this.coverLetter = coverLetter;
    }

    public UUID getJobId() { return jobId; }
    public UUID getApplicantUserId() { return applicantUserId; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String v) { this.coverLetter = v; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus v) { this.status = v; }
    public Integer getMatchScore() { return matchScore; }
    public void setMatchScore(Integer v) { this.matchScore = v; }
}
