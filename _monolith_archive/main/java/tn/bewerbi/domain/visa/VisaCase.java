package tn.bewerbi.domain.visa;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "visa_cases", indexes = {
        @Index(name = "idx_visa_user", columnList = "user_id")
})
public class VisaCase extends BaseEntity {

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visa_type", nullable = false, length = 32)
    private VisaType visaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VisaStage stage = VisaStage.PREPARATION;

    @Column(name = "appointment_date")
    private java.time.LocalDate appointmentDate;

    @Column(name = "embassy_city", length = 80)
    private String embassyCity;

    @OneToMany(mappedBy = "visaCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisaRequirement> requirements = new ArrayList<>();

    protected VisaCase() {}

    public VisaCase(UUID userId, VisaType visaType) {
        this.userId = userId;
        this.visaType = visaType;
    }

    public UUID getUserId() { return userId; }
    public VisaType getVisaType() { return visaType; }
    public void setVisaType(VisaType v) { this.visaType = v; }
    public VisaStage getStage() { return stage; }
    public void setStage(VisaStage v) { this.stage = v; }
    public java.time.LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(java.time.LocalDate v) { this.appointmentDate = v; }
    public String getEmbassyCity() { return embassyCity; }
    public void setEmbassyCity(String v) { this.embassyCity = v; }
    public List<VisaRequirement> getRequirements() { return requirements; }
}
