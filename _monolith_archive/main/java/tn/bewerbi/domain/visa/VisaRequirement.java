package tn.bewerbi.domain.visa;

import jakarta.persistence.*;
import java.time.Instant;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "visa_requirements", indexes = {
        @Index(name = "idx_visa_req_case", columnList = "case_id")
})
public class VisaRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private VisaCase visaCase;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "document_id", columnDefinition = "uuid")
    private java.util.UUID documentId;

    protected VisaRequirement() {}

    public VisaRequirement(VisaCase c, String title, int sortOrder, boolean required) {
        this.visaCase = c;
        this.title = title;
        this.sortOrder = sortOrder;
        this.required = required;
    }

    public VisaCase getVisaCase() { return visaCase; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public boolean isRequired() { return required; }
    public int getSortOrder() { return sortOrder; }
    public Instant getCompletedAt() { return completedAt; }
    public boolean isCompleted() { return completedAt != null; }
    public void markComplete() { this.completedAt = Instant.now(); }
    public void reopen() { this.completedAt = null; }
    public java.util.UUID getDocumentId() { return documentId; }
    public void setDocumentId(java.util.UUID v) { this.documentId = v; }
}
