package tn.bewerbi.visa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.visa.*;

/**
 * Tracker für den Visumsprozess (Tunesien → Deutschland).
 * Enthält Standard-Checklisten je Visum-Typ nach AufenthG.
 */
@RestController
@RequestMapping("/api/v1/visa")
@Tag(name = "Visa")
@PreAuthorize("isAuthenticated()")
public class VisaController {

    private final VisaService service;

    public VisaController(VisaService service) { this.service = service; }

    @GetMapping("/me")
    @Operation(summary = "Get current user's visa case")
    public VisaResponse me() {
        return service.me(CurrentUser.id());
    }

    @PostMapping
    @Operation(summary = "Create or replace visa case with a pre-seeded requirement checklist")
    public VisaResponse create(@Valid @RequestBody CreateRequest req) {
        return service.create(CurrentUser.id(), req);
    }

    @PatchMapping
    public VisaResponse update(@RequestBody UpdateRequest req) {
        return service.update(CurrentUser.id(), req);
    }

    @PatchMapping("/requirements/{id}/toggle")
    public VisaResponse toggleRequirement(@PathVariable UUID id) {
        return service.toggleRequirement(CurrentUser.id(), id);
    }

    public record CreateRequest(@NotNull VisaType visaType, String embassyCity) {}

    public record UpdateRequest(VisaStage stage, LocalDate appointmentDate, String embassyCity) {}

    public record RequirementResponse(UUID id, String title, String description, boolean required,
                                      int sortOrder, boolean completed, Instant completedAt,
                                      UUID documentId) {}

    public record VisaResponse(
            UUID id, VisaType visaType, VisaStage stage,
            LocalDate appointmentDate, String embassyCity,
            int progressPercent, List<RequirementResponse> requirements) {}

    @Service
    @Transactional
    public static class VisaService {

        private final VisaRepository repo;

        public VisaService(VisaRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public VisaResponse me(UUID userId) {
            return repo.findByUserId(userId).map(this::toResponse).orElse(null);
        }

        public VisaResponse create(UUID userId, CreateRequest req) {
            repo.findByUserId(userId).ifPresent(existing -> repo.delete(existing));
            var c = new VisaCase(userId, req.visaType());
            c.setEmbassyCity(req.embassyCity());
            seedRequirements(c);
            repo.save(c);
            return toResponse(c);
        }

        public VisaResponse update(UUID userId, UpdateRequest req) {
            var c = repo.findByUserId(userId).orElseThrow(() -> ResourceNotFoundException.of("VisaCase", userId));
            if (req.stage() != null) c.setStage(req.stage());
            if (req.appointmentDate() != null) c.setAppointmentDate(req.appointmentDate());
            if (req.embassyCity() != null) c.setEmbassyCity(req.embassyCity());
            return toResponse(c);
        }

        public VisaResponse toggleRequirement(UUID userId, UUID reqId) {
            var c = repo.findByUserId(userId).orElseThrow(() -> ResourceNotFoundException.of("VisaCase", userId));
            var req = c.getRequirements().stream()
                    .filter(r -> r.getId().equals(reqId))
                    .findFirst()
                    .orElseThrow(() -> ResourceNotFoundException.of("Requirement", reqId));
            if (req.isCompleted()) req.reopen();
            else req.markComplete();
            return toResponse(c);
        }

        private void seedRequirements(VisaCase c) {
            // Universelle Anforderungen
            add(c, 1, "Gültiger Reisepass", "Noch mind. 6 Monate gültig nach geplanter Einreise.", true);
            add(c, 2, "Biometrische Passfotos", "Zwei aktuelle biometrische Passfotos (35 × 45 mm).", true);
            add(c, 3, "Visumsantrag (Videx)", "Ausgefüllter Antrag über videx.diplo.de.", true);
            add(c, 4, "Krankenversicherung", "Nachweis einer Auslandskrankenversicherung / späteren deutschen KV.", true);

            switch (c.getVisaType()) {
                case BLUE_CARD -> {
                    add(c, 5, "Arbeitsvertrag mit Mindestgehalt", "Brutto ≥ 45.300 € (2024) bzw. 41.041 € Engpassberuf.", true);
                    add(c, 6, "Hochschulabschluss anerkannt", "Über anabin als H+ bewertet oder deutsche Anerkennung.", true);
                    add(c, 7, "Zustimmung Bundesagentur für Arbeit", "Bei einigen Berufen erforderlich.", false);
                }
                case SKILLED_WORKER_VOCATIONAL -> {
                    add(c, 5, "Anerkennungsbescheid", "Gleichwertigkeit der Berufsausbildung nachgewiesen.", true);
                    add(c, 6, "Konkreter Arbeitsvertrag", "Arbeitsvertrag / verbindliches Jobangebot aus Deutschland.", true);
                    add(c, 7, "Deutschkenntnisse", "In der Regel B1, je nach Beruf ggf. B2.", true);
                }
                case SKILLED_WORKER_ACADEMIC -> {
                    add(c, 5, "Anerkannter Hochschulabschluss", "Über anabin geprüft (H+).", true);
                    add(c, 6, "Arbeitsvertrag passend zur Qualifikation", "Stelle muss der akademischen Ausbildung entsprechen.", true);
                }
                case VOCATIONAL_TRAINING -> {
                    add(c, 5, "Ausbildungsvertrag", "Schul-/Betriebsvertrag mit einer deutschen Einrichtung.", true);
                    add(c, 6, "Deutschkenntnisse B1", "Nachweis über ein anerkanntes Zertifikat.", true);
                    add(c, 7, "Finanzierungsnachweis", "Sperrkonto oder Verpflichtungserklärung.", true);
                }
                case STUDY -> {
                    add(c, 5, "Zulassung / Studienplatznachweis", "Uni-Assist bzw. Hochschule.", true);
                    add(c, 6, "Finanzierungsnachweis Sperrkonto", "~11.904 € (2024) auf einem Sperrkonto.", true);
                    add(c, 7, "Deutsch- oder Englischkenntnisse", "Je nach Studiengang (TestDaF/DSH oder IELTS).", true);
                }
                case JOB_SEEKER -> {
                    add(c, 5, "Hochschulabschluss (H+ anabin)", "Oder gleichwertige berufliche Qualifikation.", true);
                    add(c, 6, "Finanzierung für ≥ 6 Monate", "Sperrkonto oder vergleichbarer Nachweis.", true);
                }
                case RECOGNITION -> {
                    add(c, 5, "Teilanerkennungsbescheid", "Mit konkret benannter Ausgleichsmaßnahme.", true);
                    add(c, 6, "Nachweis Anpassungslehrgang / Prüfung", "Ort & Träger der Ausgleichsmaßnahme.", true);
                }
                case CHANCENKARTE -> {
                    add(c, 5, "Punktesystem ≥ 6 Punkte", "Deutschkenntnisse, Alter, Berufserfahrung, Deutschland-Bezug.", true);
                    add(c, 6, "Finanzierungsnachweis ≥ 12 Monate", "Ca. 1027 €/Monat × 12 = 12.324 €.", true);
                }
            }
        }

        private void add(VisaCase c, int order, String title, String desc, boolean required) {
            var r = new VisaRequirement(c, title, order, required);
            r.setDescription(desc);
            c.getRequirements().add(r);
        }

        private VisaResponse toResponse(VisaCase c) {
            var reqs = c.getRequirements().stream()
                    .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                    .toList();
            long totalRequired = reqs.stream().filter(VisaRequirement::isRequired).count();
            long doneRequired = reqs.stream().filter(r -> r.isRequired() && r.isCompleted()).count();
            int progress = totalRequired == 0 ? 0 : (int) Math.round((100.0 * doneRequired) / totalRequired);
            var mapped = reqs.stream().map(r -> new RequirementResponse(
                    r.getId(), r.getTitle(), r.getDescription(), r.isRequired(), r.getSortOrder(),
                    r.isCompleted(), r.getCompletedAt(), r.getDocumentId())).toList();
            return new VisaResponse(c.getId(), c.getVisaType(), c.getStage(),
                    c.getAppointmentDate(), c.getEmbassyCity(), progress, mapped);
        }
    }
}
