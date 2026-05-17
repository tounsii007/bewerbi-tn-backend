package tn.bewerbi.domain.anerkennung;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

/**
 * Case for the German professional qualification recognition process ("Anerkennung").
 * Each applicant can have at most one active case.
 */
@Entity
@Table(name = "anerkennung_cases", indexes = {
        @Index(name = "idx_anerkennung_user", columnList = "user_id")
})
public class AnerkennungCase extends BaseEntity {

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String profession; // e.g. "Krankenpfleger/in", "Elektriker"

    @Enumerated(EnumType.STRING)
    @Column(name = "regulation_type", nullable = false, length = 32)
    private RegulationType regulationType = RegulationType.UNKNOWN;

    @Column(name = "competent_authority", length = 200)
    private String competentAuthority; // IHK, ZAB, Landesbehörde …

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AnerkennungStage stage = AnerkennungStage.INFORMATION;

    @OneToMany(mappedBy = "anerkennungCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnerkennungStep> steps = new ArrayList<>();

    protected AnerkennungCase() {}

    public AnerkennungCase(UUID userId, String profession) {
        this.userId = userId;
        this.profession = profession;
    }

    public UUID getUserId() { return userId; }
    public String getProfession() { return profession; }
    public void setProfession(String v) { this.profession = v; }
    public RegulationType getRegulationType() { return regulationType; }
    public void setRegulationType(RegulationType v) { this.regulationType = v; }
    public String getCompetentAuthority() { return competentAuthority; }
    public void setCompetentAuthority(String v) { this.competentAuthority = v; }
    public AnerkennungStage getStage() { return stage; }
    public void setStage(AnerkennungStage v) { this.stage = v; }
    public List<AnerkennungStep> getSteps() { return steps; }
}
