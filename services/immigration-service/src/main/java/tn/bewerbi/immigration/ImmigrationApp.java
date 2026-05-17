package tn.bewerbi.immigration;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.common.i18n.LocaleContext;
import tn.bewerbi.common.i18n.MessageClient;
import tn.bewerbi.common.security.JwtSecurityConfig;
import tn.bewerbi.common.security.SecurityFilterChainRegistrar;

@SpringBootApplication
@ComponentScan(basePackages = {"tn.bewerbi.immigration", "tn.bewerbi.common.i18n"})
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class, SecurityFilterChainRegistrar.class})
@EnableJpaAuditing
public class ImmigrationApp {
    public static void main(String[] args) { SpringApplication.run(ImmigrationApp.class, args); }

    public enum RegulationType { REGULATED, NON_REGULATED, UNKNOWN }
    public enum AnerkennungStage {
        INFORMATION, DOCUMENTS_COLLECTION, APPLICATION_SUBMITTED,
        EQUIVALENCE_REVIEW, COMPENSATION_REQUIRED, COMPLETED
    }
    public enum VisaType {
        BLUE_CARD, SKILLED_WORKER_VOCATIONAL, SKILLED_WORKER_ACADEMIC,
        VOCATIONAL_TRAINING, STUDY, JOB_SEEKER, RECOGNITION, CHANCENKARTE
    }
    public enum VisaStage {
        PREPARATION, APPOINTMENT_BOOKED, DOCUMENTS_SUBMITTED,
        UNDER_REVIEW, APPROVED, REJECTED, ENTERED_GERMANY
    }

    // ─── ENTITIES ───────────────────────────────────────────────────────

    @Entity @Table(name = "anerkennung_cases") @EntityListeners(AuditingEntityListener.class)
    public static class AnerkennungCase {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "user_id", nullable = false) UUID userId;
        @Column(nullable = false) String profession;
        @Enumerated(EnumType.STRING) @Column(name = "regulation_type", nullable = false)
        RegulationType regulationType = RegulationType.UNKNOWN;
        @Column(name = "competent_authority") String competentAuthority;
        @Enumerated(EnumType.STRING) @Column(nullable = false) AnerkennungStage stage = AnerkennungStage.INFORMATION;
        @OneToMany(mappedBy = "anerkennungCase", cascade = CascadeType.ALL, orphanRemoval = true)
        List<AnerkennungStep> steps = new ArrayList<>();
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    @Entity @Table(name = "anerkennung_steps") @EntityListeners(AuditingEntityListener.class)
    public static class AnerkennungStep {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "case_id", nullable = false)
        AnerkennungCase anerkennungCase;
        @Column(name = "title_key", nullable = false) String titleKey;
        @Column(name = "desc_key") String descKey;
        @Column(name = "sort_order", nullable = false) int sortOrder;
        @Column(name = "completed_at") Instant completedAt;
        @Column(name = "document_id") UUID documentId;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    @Entity @Table(name = "visa_cases") @EntityListeners(AuditingEntityListener.class)
    public static class VisaCase {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "user_id", nullable = false) UUID userId;
        @Enumerated(EnumType.STRING) @Column(name = "visa_type", nullable = false) VisaType visaType;
        @Enumerated(EnumType.STRING) @Column(nullable = false) VisaStage stage = VisaStage.PREPARATION;
        @Column(name = "appointment_date") LocalDate appointmentDate;
        @Column(name = "embassy_city") String embassyCity;
        @OneToMany(mappedBy = "visaCase", cascade = CascadeType.ALL, orphanRemoval = true)
        List<VisaRequirement> requirements = new ArrayList<>();
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    @Entity @Table(name = "visa_requirements") @EntityListeners(AuditingEntityListener.class)
    public static class VisaRequirement {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "case_id", nullable = false)
        VisaCase visaCase;
        @Column(name = "title_key", nullable = false) String titleKey;
        @Column(name = "desc_key") String descKey;
        @Column(nullable = false) boolean required;
        @Column(name = "sort_order", nullable = false) int sortOrder;
        @Column(name = "completed_at") Instant completedAt;
        @Column(name = "document_id") UUID documentId;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    public interface AnerkennungRepo extends JpaRepository<AnerkennungCase, UUID> {
        Optional<AnerkennungCase> findByUserId(UUID userId);
    }
    public interface VisaRepo extends JpaRepository<VisaCase, UUID> {
        Optional<VisaCase> findByUserId(UUID userId);
    }

    // ─── DTOs ───────────────────────────────────────────────────────────

    public record StepResponse(UUID id, String title, String description, int sortOrder,
                               boolean completed, Instant completedAt, UUID documentId) {}
    public record AnerkennungResponse(UUID id, String profession, RegulationType regulationType,
                                      String competentAuthority, AnerkennungStage stage,
                                      int progressPercent, List<StepResponse> steps) {}
    public record AnerkennungCreate(String profession, RegulationType regulationType) {}

    public record RequirementResponse(UUID id, String title, String description, boolean required,
                                      int sortOrder, boolean completed, Instant completedAt, UUID documentId) {}
    public record VisaResponse(UUID id, VisaType visaType, VisaStage stage,
                               LocalDate appointmentDate, String embassyCity,
                               int progressPercent, List<RequirementResponse> requirements) {}
    public record VisaCreate(VisaType visaType, String embassyCity) {}
    public record VisaUpdate(VisaStage stage, LocalDate appointmentDate, String embassyCity) {}

    // ─── CONTROLLERS ─────────────────────────────────────────────────────

    @RestController @RequestMapping("/api/v1/anerkennung") @PreAuthorize("isAuthenticated()")
    public static class AnerkennungController {
        private final AnerkennungService svc;
        public AnerkennungController(AnerkennungService svc) { this.svc = svc; }

        @GetMapping("/me") public AnerkennungResponse me() { return svc.me(CurrentUser.id()); }
        @PostMapping public AnerkennungResponse create(@RequestBody AnerkennungCreate r) {
            return svc.create(CurrentUser.id(), r);
        }
        @PatchMapping("/steps/{stepId}/toggle") public AnerkennungResponse toggle(@PathVariable UUID stepId) {
            return svc.toggle(CurrentUser.id(), stepId);
        }
    }

    @RestController @RequestMapping("/api/v1/visa") @PreAuthorize("isAuthenticated()")
    public static class VisaController {
        private final VisaService svc;
        public VisaController(VisaService svc) { this.svc = svc; }

        @GetMapping("/me") public VisaResponse me() { return svc.me(CurrentUser.id()); }
        @PostMapping public VisaResponse create(@RequestBody VisaCreate r) { return svc.create(CurrentUser.id(), r); }
        @PatchMapping public VisaResponse update(@RequestBody VisaUpdate r) { return svc.update(CurrentUser.id(), r); }
        @PatchMapping("/requirements/{id}/toggle") public VisaResponse toggle(@PathVariable UUID id) {
            return svc.toggle(CurrentUser.id(), id);
        }
    }

    // ─── SERVICES ────────────────────────────────────────────────────────

    @Service @Transactional
    public static class AnerkennungService {
        private final AnerkennungRepo repo;
        private final MessageClient messages;
        public AnerkennungService(AnerkennungRepo repo, MessageClient messages) {
            this.repo = repo; this.messages = messages;
        }

        public AnerkennungResponse me(UUID userId) {
            return repo.findByUserId(userId).map(this::to).orElse(null);
        }

        public AnerkennungResponse create(UUID userId, AnerkennungCreate r) {
            repo.findByUserId(userId).ifPresent(repo::delete);
            var c = new AnerkennungCase();
            c.userId = userId;
            c.profession = r.profession();
            if (r.regulationType() != null) c.regulationType = r.regulationType();
            c.competentAuthority = inferAuthority(r.profession(), c.regulationType);
            seed(c);
            return to(repo.save(c));
        }

        public AnerkennungResponse toggle(UUID userId, UUID stepId) {
            var c = repo.findByUserId(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("AnerkennungCase", userId));
            var s = c.steps.stream().filter(x -> x.id.equals(stepId)).findFirst()
                    .orElseThrow(() -> ResourceNotFoundException.of("Step", stepId));
            s.completedAt = s.completedAt == null ? Instant.now() : null;
            advance(c);
            return to(c);
        }

        private void seed(AnerkennungCase c) {
            addStep(c, 1, "anerkennung.step.1.title", "anerkennung.step.1.desc");
            addStep(c, 2, "anerkennung.step.2.title", "anerkennung.step.2.desc");
            addStep(c, 3, "anerkennung.step.3.title", "anerkennung.step.3.desc");
            addStep(c, 4, "anerkennung.step.4.title", "anerkennung.step.4.desc");
            addStep(c, 5, "anerkennung.step.5.title", "anerkennung.step.5.desc");
            addStep(c, 6, "anerkennung.step.6.title", "anerkennung.step.6.desc");
        }

        private void addStep(AnerkennungCase c, int order, String titleKey, String descKey) {
            var s = new AnerkennungStep();
            s.anerkennungCase = c; s.sortOrder = order; s.titleKey = titleKey; s.descKey = descKey;
            c.steps.add(s);
        }

        private String inferAuthority(String profession, RegulationType r) {
            if (profession == null) return null;
            String p = profession.toLowerCase();
            if (p.contains("pfleg") || p.contains("kranken") || p.contains("arzt") || p.contains("ärzt")) {
                return messages.resolve("anerkennung.authority.health");
            }
            if (p.contains("elektr") || p.contains("maurer") || p.contains("schlosser")) {
                return messages.resolve("anerkennung.authority.handwerk");
            }
            if (r == RegulationType.NON_REGULATED) return messages.resolve("anerkennung.authority.ihkfosa");
            return messages.resolve("anerkennung.authority.zab");
        }

        private void advance(AnerkennungCase c) {
            long done = c.steps.stream().filter(s -> s.completedAt != null).count();
            c.stage = switch ((int) done) {
                case 0 -> AnerkennungStage.INFORMATION;
                case 1 -> AnerkennungStage.DOCUMENTS_COLLECTION;
                case 2 -> AnerkennungStage.APPLICATION_SUBMITTED;
                case 3 -> AnerkennungStage.EQUIVALENCE_REVIEW;
                case 4 -> AnerkennungStage.COMPENSATION_REQUIRED;
                default -> AnerkennungStage.COMPLETED;
            };
        }

        private AnerkennungResponse to(AnerkennungCase c) {
            String locale = LocaleContext.currentTag();
            int total = c.steps.size();
            long done = c.steps.stream().filter(s -> s.completedAt != null).count();
            int progress = total == 0 ? 0 : (int) Math.round(100.0 * done / total);
            var steps = c.steps.stream()
                    .sorted(Comparator.comparingInt(s -> s.sortOrder))
                    .map(s -> new StepResponse(s.id,
                            messages.resolveIn(locale, s.titleKey),
                            s.descKey == null ? null : messages.resolveIn(locale, s.descKey),
                            s.sortOrder, s.completedAt != null, s.completedAt, s.documentId))
                    .toList();
            return new AnerkennungResponse(c.id, c.profession, c.regulationType,
                    c.competentAuthority, c.stage, progress, steps);
        }
    }

    @Service @Transactional
    public static class VisaService {
        private final VisaRepo repo;
        private final MessageClient messages;
        public VisaService(VisaRepo repo, MessageClient messages) { this.repo = repo; this.messages = messages; }

        public VisaResponse me(UUID userId) {
            return repo.findByUserId(userId).map(this::to).orElse(null);
        }

        public VisaResponse create(UUID userId, VisaCreate r) {
            repo.findByUserId(userId).ifPresent(repo::delete);
            var c = new VisaCase();
            c.userId = userId; c.visaType = r.visaType(); c.embassyCity = r.embassyCity();
            seed(c);
            return to(repo.save(c));
        }

        public VisaResponse update(UUID userId, VisaUpdate r) {
            var c = repo.findByUserId(userId).orElseThrow(() -> ResourceNotFoundException.of("VisaCase", userId));
            if (r.stage() != null) c.stage = r.stage();
            if (r.appointmentDate() != null) c.appointmentDate = r.appointmentDate();
            if (r.embassyCity() != null) c.embassyCity = r.embassyCity();
            return to(c);
        }

        public VisaResponse toggle(UUID userId, UUID reqId) {
            var c = repo.findByUserId(userId).orElseThrow(() -> ResourceNotFoundException.of("VisaCase", userId));
            var req = c.requirements.stream().filter(r -> r.id.equals(reqId)).findFirst()
                    .orElseThrow(() -> ResourceNotFoundException.of("Requirement", reqId));
            req.completedAt = req.completedAt == null ? Instant.now() : null;
            return to(c);
        }

        private void seed(VisaCase c) {
            addReq(c, 1, "visa.req.passport.title", "visa.req.passport.desc", true);
            addReq(c, 2, "visa.req.photos.title", "visa.req.photos.desc", true);
            addReq(c, 3, "visa.req.videx.title", "visa.req.videx.desc", true);
            addReq(c, 4, "visa.req.insurance.title", "visa.req.insurance.desc", true);

            int offset = 5;
            switch (c.visaType) {
                case BLUE_CARD -> {
                    addReq(c, offset++, "visa.bluecard.salary.title", "visa.bluecard.salary.desc", true);
                    addReq(c, offset++, "visa.bluecard.degree.title", "visa.bluecard.degree.desc", true);
                    addReq(c, offset,   "visa.bluecard.ba.title",     "visa.bluecard.ba.desc",     false);
                }
                case STUDY -> {
                    addReq(c, offset++, "visa.study.admission.title", "visa.study.admission.desc", true);
                    addReq(c, offset++, "visa.study.blocked.title",   "visa.study.blocked.desc",   true);
                    addReq(c, offset,   "visa.study.language.title",  "visa.study.language.desc",  true);
                }
                case VOCATIONAL_TRAINING -> {
                    addReq(c, offset++, "visa.voc.contract.title",  "visa.voc.contract.desc",  true);
                    addReq(c, offset++, "visa.voc.german.title",    "visa.voc.german.desc",    true);
                    addReq(c, offset,   "visa.voc.financing.title", "visa.voc.financing.desc", true);
                }
                case SKILLED_WORKER_VOCATIONAL -> {
                    addReq(c, offset++, "visa.skilledVoc.recognition.title", "visa.skilledVoc.recognition.desc", true);
                    addReq(c, offset++, "visa.skilledVoc.contract.title",    "visa.skilledVoc.contract.desc",    true);
                    addReq(c, offset,   "visa.skilledVoc.german.title",      "visa.skilledVoc.german.desc",      true);
                }
                case SKILLED_WORKER_ACADEMIC -> {
                    addReq(c, offset++, "visa.skilledAcad.degree.title",   "visa.skilledAcad.degree.desc",   true);
                    addReq(c, offset,   "visa.skilledAcad.contract.title", "visa.skilledAcad.contract.desc", true);
                }
                case JOB_SEEKER -> {
                    addReq(c, offset++, "visa.jobseeker.degree.title",    "visa.jobseeker.degree.desc",    true);
                    addReq(c, offset,   "visa.jobseeker.financing.title", "visa.jobseeker.financing.desc", true);
                }
                case RECOGNITION -> {
                    addReq(c, offset++, "visa.recognition.decision.title", "visa.recognition.decision.desc", true);
                    addReq(c, offset,   "visa.recognition.course.title",   "visa.recognition.course.desc",   true);
                }
                case CHANCENKARTE -> {
                    addReq(c, offset++, "visa.chance.points.title",    "visa.chance.points.desc",    true);
                    addReq(c, offset,   "visa.chance.financing.title", "visa.chance.financing.desc", true);
                }
            }
        }

        private void addReq(VisaCase c, int order, String titleKey, String descKey, boolean required) {
            var r = new VisaRequirement();
            r.visaCase = c; r.sortOrder = order; r.titleKey = titleKey; r.descKey = descKey; r.required = required;
            c.requirements.add(r);
        }

        private VisaResponse to(VisaCase c) {
            String locale = LocaleContext.currentTag();
            var reqs = c.requirements.stream().sorted(Comparator.comparingInt(r -> r.sortOrder)).toList();
            long totalReq = reqs.stream().filter(r -> r.required).count();
            long doneReq = reqs.stream().filter(r -> r.required && r.completedAt != null).count();
            int progress = totalReq == 0 ? 0 : (int) Math.round(100.0 * doneReq / totalReq);
            var mapped = reqs.stream().map(r -> new RequirementResponse(r.id,
                    messages.resolveIn(locale, r.titleKey),
                    r.descKey == null ? null : messages.resolveIn(locale, r.descKey),
                    r.required, r.sortOrder, r.completedAt != null, r.completedAt, r.documentId)).toList();
            return new VisaResponse(c.id, c.visaType, c.stage, c.appointmentDate, c.embassyCity, progress, mapped);
        }
    }
}
