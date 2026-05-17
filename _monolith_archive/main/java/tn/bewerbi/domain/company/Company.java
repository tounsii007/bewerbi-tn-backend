package tn.bewerbi.domain.company;

import jakarta.persistence.*;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_slug", columnList = "slug", unique = true),
        @Index(name = "idx_companies_owner", columnList = "owner_user_id")
})
public class Company extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false, columnDefinition = "uuid")
    private UUID ownerUserId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 80)
    private String industry;

    @Column(length = 80)
    private String size;

    @Column(length = 80)
    private String country;

    @Column(length = 80)
    private String city;

    @Column(name = "trade_register_number", length = 80)
    private String tradeRegisterNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "verification_note", length = 500)
    private String verificationNote;

    @Column(name = "rating_avg")
    private Double ratingAvg;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount = 0;

    protected Company() {}

    public Company(UUID ownerUserId, String name, String slug) {
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.slug = slug;
    }

    public UUID getOwnerUserId() { return ownerUserId; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getWebsite() { return website; }
    public void setWebsite(String v) { this.website = v; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String v) { this.logoUrl = v; }
    public String getIndustry() { return industry; }
    public void setIndustry(String v) { this.industry = v; }
    public String getSize() { return size; }
    public void setSize(String v) { this.size = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public String getTradeRegisterNumber() { return tradeRegisterNumber; }
    public void setTradeRegisterNumber(String v) { this.tradeRegisterNumber = v; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus v) { this.verificationStatus = v; }
    public String getVerificationNote() { return verificationNote; }
    public void setVerificationNote(String v) { this.verificationNote = v; }
    public Double getRatingAvg() { return ratingAvg; }
    public int getRatingCount() { return ratingCount; }

    public void updateRating(double avg, int count) {
        this.ratingAvg = avg;
        this.ratingCount = count;
    }
}
