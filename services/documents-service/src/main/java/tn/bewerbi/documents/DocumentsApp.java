package tn.bewerbi.documents;

import jakarta.persistence.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartFile;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.api.exception.BadRequestException;
import tn.bewerbi.common.security.JwtSecurityConfig;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class})
@EnableJpaAuditing
public class DocumentsApp {
    public static void main(String[] args) { SpringApplication.run(DocumentsApp.class, args); }

    public enum DocumentType {
        CV, DIPLOMA, CERTIFICATE, TRANSCRIPT, LANGUAGE_CERTIFICATE,
        PASSPORT, BIRTH_CERTIFICATE, OTHER
    }

    @Entity @Table(name = "documents") @EntityListeners(AuditingEntityListener.class)
    public static class Document {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "owner_user_id", nullable = false) UUID ownerUserId;
        @Enumerated(EnumType.STRING) @Column(nullable = false) DocumentType type;
        @Column(nullable = false) String name;
        @Column(name = "storage_path", nullable = false) String storagePath;
        @Column(name = "content_type") String contentType;
        @Column(name = "size_bytes") Long sizeBytes;
        @Column(name = "parsed_text", columnDefinition = "text") String parsedText;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    public interface DocumentRepo extends JpaRepository<Document, UUID> {
        List<Document> findByOwnerUserId(UUID userId);
    }

    public record DocumentResponse(UUID id, DocumentType type, String name, String contentType,
                                   Long sizeBytes, boolean hasParsedText, Instant createdAt) {}
    public record ParsedResponse(UUID id, DocumentType type, String parsedText) {}
    public record CvHints(String email, String phone, String germanLevel,
                          List<String> skills, List<String> languages) {}

    @Bean
    public SecurityFilterChain docsChain(HttpSecurity http, JwtAuthenticationConverter c,
                                         UrlBasedCorsConfigurationSource cors) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(x -> x.configurationSource(cors))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                    .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(c)));
        return http.build();
    }

    @RestController @RequestMapping("/api/v1/documents") @PreAuthorize("isAuthenticated()")
    public static class DocumentsController {
        private final DocService svc;
        public DocumentsController(DocService svc) { this.svc = svc; }

        @GetMapping public List<DocumentResponse> list() { return svc.list(CurrentUser.id()); }

        @PostMapping
        public DocumentResponse upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam("type") DocumentType type) throws IOException {
            return svc.upload(CurrentUser.id(), file, type);
        }

        @GetMapping("/{id}/parsed")
        public ParsedResponse parsed(@PathVariable UUID id) {
            return svc.parsed(CurrentUser.id(), id);
        }

        @DeleteMapping("/{id}")
        public void delete(@PathVariable UUID id) { svc.delete(CurrentUser.id(), id); }
    }

    @RestController @RequestMapping("/api/v1/cv") @PreAuthorize("isAuthenticated()")
    public static class CvAutofillController {
        private final CvAutofillSvc svc;
        public CvAutofillController(CvAutofillSvc svc) { this.svc = svc; }

        @PostMapping("/{documentId}/autofill")
        public CvHints autofill(@PathVariable UUID documentId) {
            return svc.extract(CurrentUser.id(), documentId);
        }
    }

    @Service @Transactional
    public static class DocService {
        private final DocumentRepo repo;
        @Value("${bewerbi.upload.root}") private String uploadRoot;

        public DocService(DocumentRepo repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public List<DocumentResponse> list(UUID userId) {
            return repo.findByOwnerUserId(userId).stream().map(this::to).toList();
        }

        public DocumentResponse upload(UUID userId, MultipartFile file, DocumentType type) throws IOException {
            if (file.isEmpty()) throw new BadRequestException("Empty file", "error.documents.empty");
            Path root = Path.of(uploadRoot).resolve(userId.toString());
            Files.createDirectories(root);
            String safe = file.getOriginalFilename() == null ? "file"
                    : file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_");
            UUID docId = UUID.randomUUID();
            Path target = root.resolve(docId + "_" + safe);
            file.transferTo(target);

            var d = new Document();
            d.id = docId; d.ownerUserId = userId; d.type = type;
            d.name = safe; d.storagePath = target.toString();
            d.contentType = file.getContentType(); d.sizeBytes = file.getSize();
            if (type == DocumentType.CV && isPdf(file.getContentType(), safe)) {
                try {
                    try (var pdf = Loader.loadPDF(target.toFile())) {
                        d.parsedText = new PDFTextStripper().getText(pdf);
                    }
                } catch (Exception ignored) {}
            }
            return to(repo.save(d));
        }

        @Transactional(readOnly = true)
        public ParsedResponse parsed(UUID userId, UUID id) {
            var d = repo.findById(id).orElseThrow();
            if (!d.ownerUserId.equals(userId)) throw new BadRequestException("Not your document", "error.documents.notOwner");
            return new ParsedResponse(d.id, d.type, d.parsedText);
        }

        public void delete(UUID userId, UUID id) {
            var d = repo.findById(id).orElse(null);
            if (d == null || !d.ownerUserId.equals(userId)) return;
            try { Files.deleteIfExists(Path.of(d.storagePath)); } catch (IOException ignored) {}
            repo.delete(d);
        }

        private DocumentResponse to(Document d) {
            return new DocumentResponse(d.id, d.type, d.name, d.contentType, d.sizeBytes,
                    d.parsedText != null && !d.parsedText.isBlank(), d.createdAt);
        }

        private static boolean isPdf(String ct, String name) {
            return "application/pdf".equalsIgnoreCase(ct) || (name != null && name.toLowerCase().endsWith(".pdf"));
        }
    }

    @Service
    public static class CvAutofillSvc {
        private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        private static final Pattern PHONE = Pattern.compile("(\\+?\\d[\\d .()-]{7,}\\d)");
        private static final Pattern GERMAN = Pattern.compile("(?i)\\b(A1|A2|B1|B2|C1|C2)\\b");
        private static final Set<String> DICT = Set.of(
                "java", "kotlin", "python", "javascript", "typescript", "react", "angular", "vue",
                "spring", "spring boot", "node.js", "flutter", "swift", "docker", "kubernetes",
                "postgresql", "mysql", "mongodb", "aws", "azure", "gcp", "git",
                "pflege", "altenpflege", "krankenpflege", "erste hilfe",
                "lkw", "eu-führerschein", "cnc", "elektro");

        private final DocumentRepo repo;
        public CvAutofillSvc(DocumentRepo repo) { this.repo = repo; }

        public CvHints extract(UUID userId, UUID docId) {
            var d = repo.findById(docId).orElseThrow();
            if (!d.ownerUserId.equals(userId)) throw new BadRequestException("Not your document", "error.documents.notOwner");
            if (d.type != DocumentType.CV) throw new BadRequestException("Not a CV", "error.documents.notCv");
            String text = Optional.ofNullable(d.parsedText).orElse("");
            if (text.isBlank()) return new CvHints(null, null, null, List.of(), List.of());

            return new CvHints(
                    first(EMAIL, text), first(PHONE, text),
                    highestLevel(text),
                    matchDict(text),
                    langLines(text));
        }

        private static String first(Pattern p, String t) {
            var m = p.matcher(t); return m.find() ? m.group().trim() : null;
        }
        private static String highestLevel(String t) {
            var m = GERMAN.matcher(t); String best = null;
            while (m.find()) {
                String v = m.group(1).toUpperCase();
                if (best == null || v.compareTo(best) > 0) best = v;
            }
            return best;
        }
        private static List<String> matchDict(String t) {
            String lower = t.toLowerCase();
            var out = new LinkedHashSet<String>();
            for (String s : DICT) if (lower.contains(s)) out.add(cap(s));
            return new ArrayList<>(out);
        }
        private static List<String> langLines(String t) {
            var out = new ArrayList<String>();
            for (String l : t.split("\\r?\\n")) {
                String s = l.toLowerCase();
                if ((s.contains("sprach") || s.contains("langu") || s.contains("langue")) && l.length() < 140) {
                    out.add(l.trim());
                }
            }
            return out;
        }
        private static String cap(String s) { return s.isEmpty() ? s : s.substring(0,1).toUpperCase() + s.substring(1); }
    }
}
