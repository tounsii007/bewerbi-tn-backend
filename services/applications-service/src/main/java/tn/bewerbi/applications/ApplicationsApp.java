package tn.bewerbi.applications;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.api.exception.ConflictException;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.common.events.DomainEvents;
import tn.bewerbi.common.events.EventPublisher;
import tn.bewerbi.common.events.Topics;
import tn.bewerbi.common.security.JwtSecurityConfig;
import tn.bewerbi.common.security.SecurityFilterChainRegistrar;

@SpringBootApplication
@ComponentScan(basePackages = {
        "tn.bewerbi.applications", "tn.bewerbi.common.i18n", "tn.bewerbi.common.events"
})
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class, SecurityFilterChainRegistrar.class})
@EnableJpaAuditing
public class ApplicationsApp {
    public static void main(String[] args) { SpringApplication.run(ApplicationsApp.class, args); }

    public enum ApplicationStatus { PENDING, REVIEWED, INTERVIEW, ACCEPTED, REJECTED, WITHDRAWN }

    @Entity @Table(name = "applications") @EntityListeners(AuditingEntityListener.class)
    public static class Application {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "job_id", nullable = false) UUID jobId;
        @Column(name = "applicant_user_id", nullable = false) UUID applicantUserId;
        @Column(columnDefinition = "text") String coverLetter;
        @Enumerated(EnumType.STRING) @Column(nullable = false) ApplicationStatus status = ApplicationStatus.PENDING;
        @Column(name = "match_score") Integer matchScore;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    @Entity @Table(name = "favorites") @EntityListeners(AuditingEntityListener.class)
    public static class Favorite {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "user_id", nullable = false) UUID userId;
        @Column(name = "job_id", nullable = false) UUID jobId;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    public interface ApplicationRepo extends JpaRepository<Application, UUID> {
        Page<Application> findByApplicantUserId(UUID userId, Pageable p);
        java.util.Optional<Application> findByJobIdAndApplicantUserId(UUID jobId, UUID userId);
    }

    public interface FavoriteRepo extends JpaRepository<Favorite, UUID> {
        List<Favorite> findByUserId(UUID userId);
        java.util.Optional<Favorite> findByUserIdAndJobId(UUID userId, UUID jobId);
        void deleteByUserIdAndJobId(UUID userId, UUID jobId);
    }

    public record ApplyRequest(UUID jobId, String coverLetter) {}
    public record ApplicationResponse(UUID id, UUID jobId, UUID applicantUserId,
                                      String coverLetter, ApplicationStatus status,
                                      Integer matchScore, Instant createdAt) {}

    @RestController @RequestMapping("/api/v1/applications") @PreAuthorize("isAuthenticated()")
    public static class ApplicationsController {
        private final ApplicationSvc svc;
        public ApplicationsController(ApplicationSvc svc) { this.svc = svc; }
        @GetMapping("/mine") public Page<ApplicationResponse> mine(Pageable p) { return svc.mine(CurrentUser.id(), p); }
        @PostMapping public ApplicationResponse apply(@RequestBody ApplyRequest r) { return svc.apply(CurrentUser.id(), r); }
        @PatchMapping("/{id}/withdraw") public ApplicationResponse withdraw(@PathVariable UUID id) {
            return svc.withdraw(CurrentUser.id(), id);
        }
    }

    @RestController @RequestMapping("/api/v1/favorites") @PreAuthorize("isAuthenticated()")
    public static class FavoritesController {
        private final FavoriteSvc svc;
        public FavoritesController(FavoriteSvc svc) { this.svc = svc; }
        @GetMapping public List<UUID> list() { return svc.list(CurrentUser.id()); }
        @PostMapping("/{jobId}") public void add(@PathVariable UUID jobId) { svc.add(CurrentUser.id(), jobId); }
        @DeleteMapping("/{jobId}") public void remove(@PathVariable UUID jobId) { svc.remove(CurrentUser.id(), jobId); }
    }

    @Service @Transactional
    public static class ApplicationSvc {
        private final ApplicationRepo repo;
        private final EventPublisher events;
        public ApplicationSvc(ApplicationRepo repo, EventPublisher events) {
            this.repo = repo; this.events = events;
        }

        @Transactional(readOnly = true)
        public Page<ApplicationResponse> mine(UUID userId, Pageable p) {
            return repo.findByApplicantUserId(userId, p).map(this::to);
        }

        public ApplicationResponse apply(UUID userId, ApplyRequest r) {
            repo.findByJobIdAndApplicantUserId(r.jobId(), userId).ifPresent(a -> {
                throw new ConflictException("Already applied", "error.applications.duplicate");
            });
            var a = new Application();
            a.jobId = r.jobId();
            a.applicantUserId = userId;
            a.coverLetter = r.coverLetter();
            var saved = repo.save(a);
            events.publish(Topics.APPLICATION_CREATED, saved.id.toString(),
                    new DomainEvents.ApplicationCreated(
                            saved.id, saved.jobId, saved.applicantUserId, java.time.Instant.now()));
            return to(saved);
        }

        public ApplicationResponse withdraw(UUID userId, UUID id) {
            var a = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Application", id));
            if (!a.applicantUserId.equals(userId)) throw ResourceNotFoundException.of("Application", id);
            a.status = ApplicationStatus.WITHDRAWN;
            return to(a);
        }

        private ApplicationResponse to(Application a) {
            return new ApplicationResponse(a.id, a.jobId, a.applicantUserId, a.coverLetter,
                    a.status, a.matchScore, a.createdAt);
        }
    }

    @Service @Transactional
    public static class FavoriteSvc {
        private final FavoriteRepo repo;
        public FavoriteSvc(FavoriteRepo repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public List<UUID> list(UUID userId) {
            return repo.findByUserId(userId).stream().map(f -> f.jobId).toList();
        }

        public void add(UUID userId, UUID jobId) {
            if (repo.findByUserIdAndJobId(userId, jobId).isPresent()) return;
            var f = new Favorite(); f.userId = userId; f.jobId = jobId;
            repo.save(f);
        }

        public void remove(UUID userId, UUID jobId) {
            repo.deleteByUserIdAndJobId(userId, jobId);
        }
    }
}
