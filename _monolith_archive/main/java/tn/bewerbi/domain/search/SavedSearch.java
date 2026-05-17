package tn.bewerbi.domain.search;

import jakarta.persistence.*;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;
import tn.bewerbi.domain.job.JobCategory;
import tn.bewerbi.domain.job.JobType;
import tn.bewerbi.domain.profile.GermanLevel;

@Entity
@Table(name = "saved_searches", indexes = {
        @Index(name = "idx_saved_searches_user", columnList = "user_id")
})
public class SavedSearch extends BaseEntity {

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "query", length = 200)
    private String query;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private JobCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private JobType type;

    @Column(length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "min_german_level", length = 4)
    private GermanLevel minGermanLevel;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "alerts_enabled", nullable = false)
    private boolean alertsEnabled = true;

    protected SavedSearch() {}

    public SavedSearch(UUID userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getQuery() { return query; }
    public void setQuery(String v) { this.query = v; }
    public JobCategory getCategory() { return category; }
    public void setCategory(JobCategory v) { this.category = v; }
    public JobType getType() { return type; }
    public void setType(JobType v) { this.type = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public GermanLevel getMinGermanLevel() { return minGermanLevel; }
    public void setMinGermanLevel(GermanLevel v) { this.minGermanLevel = v; }
    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer v) { this.salaryMin = v; }
    public boolean isAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(boolean v) { this.alertsEnabled = v; }
}
