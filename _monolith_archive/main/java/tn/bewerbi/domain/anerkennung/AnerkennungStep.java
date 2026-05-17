package tn.bewerbi.domain.anerkennung;

import jakarta.persistence.*;
import java.time.Instant;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "anerkennung_steps", indexes = {
        @Index(name = "idx_anerkennung_steps_case", columnList = "case_id")
})
public class AnerkennungStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private AnerkennungCase anerkennungCase;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "document_id", columnDefinition = "uuid")
    private java.util.UUID documentId;

    protected AnerkennungStep() {}

    public AnerkennungStep(AnerkennungCase c, String title, int sortOrder) {
        this.anerkennungCase = c;
        this.title = title;
        this.sortOrder = sortOrder;
    }

    public AnerkennungCase getAnerkennungCase() { return anerkennungCase; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public int getSortOrder() { return sortOrder; }
    public Instant getCompletedAt() { return completedAt; }
    public boolean isCompleted() { return completedAt != null; }
    public void markComplete() { this.completedAt = Instant.now(); }
    public void reopen() { this.completedAt = null; }
    public java.util.UUID getDocumentId() { return documentId; }
    public void setDocumentId(java.util.UUID v) { this.documentId = v; }
}
