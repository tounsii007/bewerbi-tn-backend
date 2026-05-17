package tn.bewerbi.domain.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByOwnerUserId(UUID userId);
    List<Document> findByOwnerUserIdAndType(UUID userId, DocumentType type);
}
