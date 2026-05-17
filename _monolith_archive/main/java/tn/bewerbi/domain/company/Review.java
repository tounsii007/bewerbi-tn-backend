package tn.bewerbi.domain.company;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "company_reviews", indexes = {
        @Index(name = "idx_reviews_company", columnList = "company_id"),
        @Index(name = "idx_reviews_author", columnList = "author_user_id")
})
public class Review extends BaseEntity {

    @Column(name = "company_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Column(name = "author_user_id", nullable = false, columnDefinition = "uuid")
    private UUID authorUserId;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(length = 120)
    private String title;

    @Column(length = 4000)
    private String body;

    @Column(name = "pros", length = 1000)
    private String pros;

    @Column(name = "cons", length = 1000)
    private String cons;

    @Column(name = "employment_status", length = 40)
    private String employmentStatus; // current, former, interview-only

    protected Review() {}

    public Review(UUID companyId, UUID authorUserId, int rating) {
        this.companyId = companyId;
        this.authorUserId = authorUserId;
        this.rating = rating;
    }

    public UUID getCompanyId() { return companyId; }
    public UUID getAuthorUserId() { return authorUserId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getBody() { return body; }
    public void setBody(String v) { this.body = v; }
    public String getPros() { return pros; }
    public void setPros(String v) { this.pros = v; }
    public String getCons() { return cons; }
    public void setCons(String v) { this.cons = v; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String v) { this.employmentStatus = v; }
}
