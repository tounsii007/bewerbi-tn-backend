package tn.bewerbi.anerkennung;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.anerkennung.*;

/**
 * Assistent für die Anerkennung der Berufsqualifikation in Deutschland.
 * Orientiert sich an https://www.anerkennung-in-deutschland.de.
 */
@RestController
@RequestMapping("/api/v1/anerkennung")
@Tag(name = "Anerkennung")
@PreAuthorize("isAuthenticated()")
public class AnerkennungController {

    private final AnerkennungService service;

    public AnerkennungController(AnerkennungService service) { this.service = service; }

    @GetMapping("/me")
    @Operation(summary = "Get current user's Anerkennung case")
    public AnerkennungResponse me() {
        return service.me(CurrentUser.id());
    }

    @PostMapping
    @Operation(summary = "Create a new Anerkennung case, pre-seeded with standard steps")
    public AnerkennungResponse create(@Valid @RequestBody CreateRequest req) {
        return service.create(CurrentUser.id(), req);
    }

    @PatchMapping("/steps/{stepId}/toggle")
    public AnerkennungResponse toggleStep(@PathVariable UUID stepId) {
        return service.toggleStep(CurrentUser.id(), stepId);
    }

    public record CreateRequest(@NotBlank String profession, RegulationType regulationType) {}

    public record StepResponse(UUID id, String title, String description, int sortOrder,
                               boolean completed, Instant completedAt, UUID documentId) {}

    public record AnerkennungResponse(
            UUID id, String profession, RegulationType regulationType,
            String competentAuthority, AnerkennungStage stage,
            int progressPercent, List<StepResponse> steps) {}

    @Service
    @Transactional
    public static class AnerkennungService {

        private final AnerkennungRepository repo;

        public AnerkennungService(AnerkennungRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public AnerkennungResponse me(UUID userId) {
            return repo.findByUserId(userId).map(this::toResponse).orElse(null);
        }

        public AnerkennungResponse create(UUID userId, CreateRequest req) {
            repo.findByUserId(userId).ifPresent(existing -> repo.delete(existing));
            var c = new AnerkennungCase(userId, req.profession());
            if (req.regulationType() != null) c.setRegulationType(req.regulationType());
            c.setCompetentAuthority(inferAuthority(req.profession(), req.regulationType()));
            seedSteps(c);
            repo.save(c);
            return toResponse(c);
        }

        public AnerkennungResponse toggleStep(UUID userId, UUID stepId) {
            var c = repo.findByUserId(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("AnerkennungCase", userId));
            var step = c.getSteps().stream()
                    .filter(s -> s.getId().equals(stepId))
                    .findFirst()
                    .orElseThrow(() -> ResourceNotFoundException.of("Step", stepId));
            if (step.isCompleted()) step.reopen();
            else step.markComplete();
            advanceStageIfPossible(c);
            return toResponse(c);
        }

        private void seedSteps(AnerkennungCase c) {
            add(c, 1, "Informationsgespräch ZAB / IHK FOSA",
                    "Kostenlose Erstberatung zur Anerkennung Ihres ausländischen Berufsabschlusses.");
            add(c, 2, "Unterlagen zusammenstellen",
                    "Originalzeugnisse, beglaubigte deutsche Übersetzungen, Lebenslauf, Identitätsnachweis.");
            add(c, 3, "Antrag auf Anerkennung stellen",
                    "Antrag bei der zuständigen Stelle einreichen (IHK FOSA / Handwerkskammer / Landesbehörde).");
            add(c, 4, "Gleichwertigkeitsprüfung abwarten",
                    "Die zuständige Stelle prüft innerhalb von ca. 3 Monaten die Gleichwertigkeit.");
            add(c, 5, "Ausgleichsmaßnahme (falls nötig)",
                    "Anpassungslehrgang, Kenntnisprüfung oder Eignungstest bei wesentlichen Unterschieden.");
            add(c, 6, "Anerkennungsbescheid erhalten",
                    "Volle, teilweise oder keine Gleichwertigkeit — inklusive Begründung.");
        }

        private void add(AnerkennungCase c, int order, String title, String desc) {
            var s = new AnerkennungStep(c, title, order);
            s.setDescription(desc);
            c.getSteps().add(s);
        }

        private void advanceStageIfPossible(AnerkennungCase c) {
            long done = c.getSteps().stream().filter(AnerkennungStep::isCompleted).count();
            AnerkennungStage next = switch ((int) done) {
                case 0 -> AnerkennungStage.INFORMATION;
                case 1 -> AnerkennungStage.DOCUMENTS_COLLECTION;
                case 2 -> AnerkennungStage.APPLICATION_SUBMITTED;
                case 3 -> AnerkennungStage.EQUIVALENCE_REVIEW;
                case 4 -> AnerkennungStage.COMPENSATION_REQUIRED;
                default -> AnerkennungStage.COMPLETED;
            };
            c.setStage(next);
        }

        private String inferAuthority(String profession, RegulationType regulation) {
            if (profession == null) return null;
            String p = profession.toLowerCase();
            if (p.contains("pfleg") || p.contains("kranken") || p.contains("arzt") || p.contains("ärzt")) {
                return "Zuständige Landesbehörde (Gesundheitsbereich)";
            }
            if (p.contains("elektr") || p.contains("schreiner") || p.contains("maurer") || p.contains("schlosser")) {
                return "Handwerkskammer";
            }
            if (regulation == RegulationType.NON_REGULATED) return "IHK FOSA (Foreign Skills Approval)";
            return "Zentrale Stelle für ausländisches Bildungswesen (ZAB)";
        }

        private AnerkennungResponse toResponse(AnerkennungCase c) {
            int total = c.getSteps().size();
            long done = c.getSteps().stream().filter(AnerkennungStep::isCompleted).count();
            int progress = total == 0 ? 0 : (int) Math.round((100.0 * done) / total);
            var steps = c.getSteps().stream()
                    .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                    .map(s -> new StepResponse(s.getId(), s.getTitle(), s.getDescription(),
                            s.getSortOrder(), s.isCompleted(), s.getCompletedAt(), s.getDocumentId()))
                    .toList();
            return new AnerkennungResponse(c.getId(), c.getProfession(), c.getRegulationType(),
                    c.getCompetentAuthority(), c.getStage(), progress, steps);
        }
    }
}
