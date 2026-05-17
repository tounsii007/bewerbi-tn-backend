package tn.bewerbi.cv;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.BadRequestException;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.document.DocumentRepository;
import tn.bewerbi.domain.document.DocumentType;
import tn.bewerbi.domain.profile.GermanLevel;

/**
 * Extracts structured hints (email, phone, German level, skills) from a parsed CV
 * so the client can auto-fill the profile form with user confirmation.
 *
 * Heuristic-based; not perfect by design — the frontend shows a diff-apply UI.
 */
@RestController
@RequestMapping("/api/v1/cv")
@Tag(name = "CV Autofill")
@PreAuthorize("isAuthenticated()")
public class CvAutofillController {

    private final CvAutofillService service;

    public CvAutofillController(CvAutofillService service) { this.service = service; }

    @PostMapping("/{documentId}/autofill")
    @Operation(summary = "Extract structured hints from a parsed CV for profile auto-fill")
    public CvHints autofill(@PathVariable java.util.UUID documentId) {
        return service.extract(CurrentUser.id(), documentId);
    }

    public record CvHints(
            String email, String phone,
            GermanLevel germanLevel,
            List<String> skills,
            List<String> languages,
            List<String> education,
            List<String> experiences) {}

    @Service
    public static class CvAutofillService {

        private static final Pattern EMAIL =
                Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        private static final Pattern PHONE =
                Pattern.compile("(\\+?\\d[\\d .()-]{7,}\\d)");
        private static final Pattern GERMAN_LEVEL =
                Pattern.compile("(?i)\\b(A1|A2|B1|B2|C1|C2)\\b");

        private static final Set<String> SKILL_DICTIONARY = Set.of(
                "java", "kotlin", "python", "javascript", "typescript", "react", "angular", "vue",
                "spring", "spring boot", "node.js", "flutter", "swift", "docker", "kubernetes",
                "postgresql", "mysql", "mongodb", "redis", "aws", "azure", "gcp", "git",
                "pflege", "altenpflege", "krankenpflege", "erste hilfe",
                "lkw", "eu-führerschein", "cnc", "elektro", "installation"
        );

        private final DocumentRepository documents;

        public CvAutofillService(DocumentRepository documents) { this.documents = documents; }

        public CvHints extract(java.util.UUID userId, java.util.UUID documentId) {
            var doc = documents.findById(documentId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Document", documentId));
            if (!doc.getOwnerUserId().equals(userId)) {
                throw new BadRequestException("Not your document");
            }
            if (doc.getType() != DocumentType.CV) {
                throw new BadRequestException("Document is not a CV");
            }
            String text = Optional.ofNullable(doc.getParsedText()).orElse("");
            if (text.isBlank()) {
                return new CvHints(null, null, null, List.of(), List.of(), List.of(), List.of());
            }

            String email = firstMatch(EMAIL, text);
            String phone = firstMatch(PHONE, text);
            GermanLevel level = extractGermanLevel(text);
            List<String> skills = extractSkills(text);
            List<String> languages = extractLanguageLines(text);
            List<String> education = extractSection(text, "(?i)(ausbildung|bildung|éducation|education)");
            List<String> experiences = extractSection(text, "(?i)(erfahrung|berufserfahrung|expérience|experience)");

            return new CvHints(email, phone, level, skills, languages, education, experiences);
        }

        private static String firstMatch(Pattern p, String text) {
            Matcher m = p.matcher(text);
            return m.find() ? m.group().trim() : null;
        }

        private static GermanLevel extractGermanLevel(String text) {
            Matcher m = GERMAN_LEVEL.matcher(text);
            GermanLevel highest = null;
            while (m.find()) {
                try {
                    GermanLevel lvl = GermanLevel.valueOf(m.group(1).toUpperCase());
                    if (highest == null || lvl.ordinal() > highest.ordinal()) highest = lvl;
                } catch (IllegalArgumentException ignored) {}
            }
            return highest;
        }

        private static List<String> extractSkills(String text) {
            String lower = text.toLowerCase();
            var found = new LinkedHashSet<String>();
            for (String s : SKILL_DICTIONARY) {
                if (lower.contains(s)) found.add(capitalize(s));
            }
            return new ArrayList<>(found);
        }

        private static List<String> extractLanguageLines(String text) {
            var result = new ArrayList<String>();
            for (String line : text.split("\\r?\\n")) {
                String l = line.toLowerCase();
                if ((l.contains("sprach") || l.contains("langu") || l.contains("langue"))
                        && line.length() < 140) {
                    result.add(line.trim());
                }
            }
            return result;
        }

        private static List<String> extractSection(String text, String headerRegex) {
            String[] lines = text.split("\\r?\\n");
            var out = new ArrayList<String>();
            boolean inside = false;
            int takenAfter = 0;
            for (String l : lines) {
                if (l.matches(".*" + headerRegex + ".*")) { inside = true; takenAfter = 0; continue; }
                if (inside) {
                    if (takenAfter++ > 6) break;
                    String t = l.trim();
                    if (!t.isEmpty()) out.add(t);
                }
            }
            return out;
        }

        private static String capitalize(String s) {
            if (s.isEmpty()) return s;
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }
}
