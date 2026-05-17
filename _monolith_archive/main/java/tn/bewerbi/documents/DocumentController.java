package tn.bewerbi.documents;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.bewerbi.api.BadRequestException;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.config.BewerbiProperties;
import tn.bewerbi.domain.document.Document;
import tn.bewerbi.domain.document.DocumentRepository;
import tn.bewerbi.domain.document.DocumentType;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents")
@PreAuthorize("isAuthenticated()")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) { this.service = service; }

    @GetMapping
    public List<DocumentResponse> list() {
        return service.list(CurrentUser.id());
    }

    @PostMapping
    @Operation(summary = "Upload a document; when type=CV the PDF text is extracted")
    public DocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type) throws IOException {
        return service.upload(CurrentUser.id(), file, type);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(CurrentUser.id(), id);
    }

    @GetMapping("/{id}/parsed")
    @Operation(summary = "Return the extracted text for a parsed CV / document")
    public ParsedResponse parsed(@PathVariable UUID id) {
        return service.parsed(CurrentUser.id(), id);
    }

    public record DocumentResponse(
            UUID id, DocumentType type, String name, String contentType, Long sizeBytes,
            boolean hasParsedText, Instant createdAt) {}

    public record ParsedResponse(UUID id, DocumentType type, String parsedText) {}

    @Service
    @Transactional
    public static class DocumentService {

        private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

        private final DocumentRepository repo;
        private final BewerbiProperties props;

        public DocumentService(DocumentRepository repo, BewerbiProperties props) {
            this.repo = repo;
            this.props = props;
        }

        @Transactional(readOnly = true)
        public List<DocumentResponse> list(UUID userId) {
            return repo.findByOwnerUserId(userId).stream().map(this::toResponse).toList();
        }

        public DocumentResponse upload(UUID userId, MultipartFile file, DocumentType type) throws IOException {
            if (file.isEmpty()) throw new BadRequestException("Empty file");
            Path root = props.upload().root().resolve(userId.toString());
            Files.createDirectories(root);
            String safe = file.getOriginalFilename() == null ? "file" :
                    file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_");
            UUID docId = UUID.randomUUID();
            Path target = root.resolve(docId + "_" + safe);
            file.transferTo(target);

            var doc = new Document(userId, type, safe, target.toString());
            doc.setContentType(file.getContentType());
            doc.setSizeBytes(file.getSize());

            if (type == DocumentType.CV && isPdf(file.getContentType(), safe)) {
                try {
                    doc.setParsedText(extractPdfText(target));
                } catch (Exception e) {
                    log.warn("PDF parse failed for {}: {}", safe, e.getMessage());
                }
            }

            return toResponse(repo.save(doc));
        }

        public void delete(UUID userId, UUID id) {
            var d = repo.findById(id).orElse(null);
            if (d == null || !d.getOwnerUserId().equals(userId)) return;
            try { Files.deleteIfExists(Path.of(d.getStoragePath())); } catch (IOException ignored) {}
            repo.delete(d);
        }

        @Transactional(readOnly = true)
        public ParsedResponse parsed(UUID userId, UUID id) {
            var d = repo.findById(id).orElseThrow();
            if (!d.getOwnerUserId().equals(userId)) throw new BadRequestException("Not your document");
            return new ParsedResponse(d.getId(), d.getType(), d.getParsedText());
        }

        private static boolean isPdf(String contentType, String name) {
            return "application/pdf".equalsIgnoreCase(contentType)
                    || (name != null && name.toLowerCase().endsWith(".pdf"));
        }

        private static String extractPdfText(Path path) throws IOException {
            try (var doc = Loader.loadPDF(path.toFile())) {
                var stripper = new PDFTextStripper();
                return stripper.getText(doc);
            }
        }

        private DocumentResponse toResponse(Document d) {
            return new DocumentResponse(d.getId(), d.getType(), d.getName(), d.getContentType(),
                    d.getSizeBytes(), d.getParsedText() != null && !d.getParsedText().isBlank(),
                    d.getCreatedAt());
        }
    }
}
