package tn.bewerbi.domain.document;

import jakarta.persistence.*;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_documents_owner", columnList = "owner_user_id")
})
public class Document extends BaseEntity {

    @Column(name = "owner_user_id", nullable = false, columnDefinition = "uuid")
    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DocumentType type;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "content_type", length = 80)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "parsed_text", columnDefinition = "text")
    private String parsedText;

    protected Document() {}

    public Document(UUID ownerUserId, DocumentType type, String name, String storagePath) {
        this.ownerUserId = ownerUserId;
        this.type = type;
        this.name = name;
        this.storagePath = storagePath;
    }

    public UUID getOwnerUserId() { return ownerUserId; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType v) { this.type = v; }
    public String getName() { return name; }
    public String getStoragePath() { return storagePath; }
    public String getContentType() { return contentType; }
    public void setContentType(String v) { this.contentType = v; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long v) { this.sizeBytes = v; }
    public String getParsedText() { return parsedText; }
    public void setParsedText(String v) { this.parsedText = v; }
}
