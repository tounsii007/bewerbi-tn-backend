package tn.bewerbi.companies;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.api.exception.ConflictException;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.common.security.JwtSecurityConfig;

@SpringBootApplication
@ComponentScan(basePackages = {"tn.bewerbi.companies", "tn.bewerbi.common.i18n"})
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class})
@EnableJpaAuditing
public class CompaniesApp {
    public static void main(String[] args) { SpringApplication.run(CompaniesApp.class, args); }

    public enum VerificationStatus { UNVERIFIED, PENDING_REVIEW, VERIFIED, REJECTED }

    @Entity @Table(name = "companies") @EntityListeners(AuditingEntityListener.class)
    public static class Company {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "owner_user_id", nullable = false) UUID ownerUserId;
        @Column(nullable = false) String name;
        @Column(nullable = false, unique = true) String slug;
        @Column(length = 2000) String description;
        String website;
        @Column(name = "logo_url") String logoUrl;
        String industry;
        String size;
        String country;
        String city;
        @Column(name = "trade_register_number") String tradeRegisterNumber;
        @Enumerated(EnumType.STRING) @Column(name = "verification_status", nullable = false)
        VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;
        @Column(name = "verification_note") String verificationNote;
        @Column(name = "rating_avg") Double ratingAvg;
        @Column(name = "rating_count", nullable = false) int ratingCount;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    @Entity @Table(name = "company_reviews") @EntityListeners(AuditingEntityListener.class)
    public static class Review {
        @Id @Column(columnDefinition = "uuid") UUID id = UUID.randomUUID();
        @Column(name = "company_id", nullable = false) UUID companyId;
        @Column(name = "author_user_id", nullable = false) UUID authorUserId;
        @Column(nullable = false) int rating;
        String title;
        @Column(length = 4000) String body;
        @Column(length = 1000) String pros;
        @Column(length = 1000) String cons;
        @Column(name = "employment_status") String employmentStatus;
        @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) Instant createdAt;
        @LastModifiedDate @Column(name = "updated_at", nullable = false) Instant updatedAt;
    }

    public interface CompanyRepo extends JpaRepository<Company, UUID> {
        java.util.Optional<Company> findBySlug(String slug);
        boolean existsBySlug(String slug);
    }

    public interface ReviewRepo extends JpaRepository<Review, UUID> {
        Page<Review> findByCompanyId(UUID companyId, Pageable p);
        @Query("select coalesce(avg(r.rating),0), count(r) from Review r where r.companyId = :id")
        Object[] aggregate(UUID id);
    }

    public record CompanyResponse(UUID id, String name, String slug, String description, String website,
                                  String logoUrl, String industry, String size, String country, String city,
                                  VerificationStatus verificationStatus, Double ratingAvg, int ratingCount) {}
    public record CompanyCreateRequest(@NotBlank String name, @NotBlank String slug, String description,
                                       String website, String logoUrl, String industry, String size,
                                       String country, String city) {}
    public record CompanyUpdateRequest(String name, String description, String website, String logoUrl,
                                       String industry, String size, String country, String city) {}
    public record VerificationRequest(String tradeRegisterNumber, String note) {}
    public record VerificationDecision(VerificationStatus status, String note) {}

    public record ReviewRequest(@Min(1) @Max(5) int rating, String title, String body,
                                String pros, String cons, String employmentStatus) {}
    public record ReviewResponse(UUID id, UUID companyId, UUID authorUserId, int rating,
                                 String title, String body, String pros, String cons,
                                 String employmentStatus, Instant createdAt) {}

    @RestController @RequestMapping("/api/v1/companies")
    public static class CompaniesController {
        private final CompanyService svc;
        public CompaniesController(CompanyService svc) { this.svc = svc; }

        @GetMapping public Page<CompanyResponse> list(Pageable p) { return svc.list(p); }
        @GetMapping("/{slug}") public CompanyResponse bySlug(@PathVariable String slug) { return svc.bySlug(slug); }

        @PostMapping @PreAuthorize("hasRole('EMPLOYER')")
        public CompanyResponse create(@Valid @RequestBody CompanyCreateRequest r) {
            return svc.create(CurrentUser.id(), r);
        }
        @PutMapping("/{id}") @PreAuthorize("hasRole('EMPLOYER')")
        public CompanyResponse update(@PathVariable UUID id, @RequestBody CompanyUpdateRequest r) {
            return svc.update(CurrentUser.id(), id, r);
        }
        @PostMapping("/{id}/verification-request") @PreAuthorize("hasRole('EMPLOYER')")
        public CompanyResponse requestVerification(@PathVariable UUID id, @RequestBody VerificationRequest r) {
            return svc.requestVerification(CurrentUser.id(), id, r);
        }
        @PostMapping("/{id}/verification-decision") @PreAuthorize("hasRole('ADMIN')")
        public CompanyResponse decide(@PathVariable UUID id, @RequestBody VerificationDecision r) {
            return svc.decide(id, r);
        }
    }

    @RestController @RequestMapping("/api/v1/companies/{companyId}/reviews")
    public static class ReviewsController {
        private final ReviewService svc;
        public ReviewsController(ReviewService svc) { this.svc = svc; }

        @GetMapping public Page<ReviewResponse> list(@PathVariable UUID companyId, Pageable p) {
            return svc.list(companyId, p);
        }

        @PostMapping @PreAuthorize("isAuthenticated()")
        public ReviewResponse create(@PathVariable UUID companyId, @Valid @RequestBody ReviewRequest r) {
            return svc.create(CurrentUser.id(), companyId, r);
        }
    }

    @Service @Transactional
    public static class CompanyService {
        private final CompanyRepo repo;
        public CompanyService(CompanyRepo repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public Page<CompanyResponse> list(Pageable p) { return repo.findAll(p).map(this::to); }

        @Transactional(readOnly = true)
        public CompanyResponse bySlug(String slug) {
            return repo.findBySlug(slug).map(this::to)
                    .orElseThrow(() -> ResourceNotFoundException.of("Company", slug));
        }

        public CompanyResponse create(UUID ownerId, CompanyCreateRequest r) {
            if (repo.existsBySlug(r.slug())) throw new ConflictException("Slug taken", "error.companies.slugTaken");
            var c = new Company();
            c.ownerUserId = ownerId; c.name = r.name(); c.slug = r.slug();
            c.description = r.description(); c.website = r.website(); c.logoUrl = r.logoUrl();
            c.industry = r.industry(); c.size = r.size(); c.country = r.country(); c.city = r.city();
            return to(repo.save(c));
        }

        public CompanyResponse update(UUID ownerId, UUID id, CompanyUpdateRequest r) {
            var c = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Company", id));
            if (!c.ownerUserId.equals(ownerId)) throw new ConflictException("Not owner", "error.companies.notOwner");
            if (r.name() != null) c.name = r.name();
            if (r.description() != null) c.description = r.description();
            if (r.website() != null) c.website = r.website();
            if (r.logoUrl() != null) c.logoUrl = r.logoUrl();
            if (r.industry() != null) c.industry = r.industry();
            if (r.size() != null) c.size = r.size();
            if (r.country() != null) c.country = r.country();
            if (r.city() != null) c.city = r.city();
            return to(c);
        }

        public CompanyResponse requestVerification(UUID ownerId, UUID id, VerificationRequest r) {
            var c = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Company", id));
            if (!c.ownerUserId.equals(ownerId)) throw new ConflictException("Not owner", "error.companies.notOwner");
            if (r.tradeRegisterNumber() != null) c.tradeRegisterNumber = r.tradeRegisterNumber();
            c.verificationStatus = VerificationStatus.PENDING_REVIEW;
            c.verificationNote = r.note();
            return to(c);
        }

        public CompanyResponse decide(UUID id, VerificationDecision r) {
            var c = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Company", id));
            c.verificationStatus = r.status();
            c.verificationNote = r.note();
            return to(c);
        }

        private CompanyResponse to(Company c) {
            return new CompanyResponse(c.id, c.name, c.slug, c.description, c.website, c.logoUrl,
                    c.industry, c.size, c.country, c.city, c.verificationStatus, c.ratingAvg, c.ratingCount);
        }
    }

    @Service @Transactional
    public static class ReviewService {
        private final ReviewRepo reviews;
        private final CompanyRepo companies;
        public ReviewService(ReviewRepo reviews, CompanyRepo companies) {
            this.reviews = reviews; this.companies = companies;
        }

        @Transactional(readOnly = true)
        public Page<ReviewResponse> list(UUID companyId, Pageable p) {
            return reviews.findByCompanyId(companyId, p).map(this::to);
        }

        public ReviewResponse create(UUID authorId, UUID companyId, ReviewRequest r) {
            var c = companies.findById(companyId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Company", companyId));
            var rev = new Review();
            rev.companyId = companyId; rev.authorUserId = authorId;
            rev.rating = r.rating(); rev.title = r.title(); rev.body = r.body();
            rev.pros = r.pros(); rev.cons = r.cons(); rev.employmentStatus = r.employmentStatus();
            reviews.save(rev);

            Object[] agg = reviews.aggregate(companyId);
            c.ratingAvg = agg[0] instanceof Number n ? n.doubleValue() : 0.0;
            c.ratingCount = agg[1] instanceof Number n ? n.intValue() : 0;
            return to(rev);
        }

        private ReviewResponse to(Review r) {
            return new ReviewResponse(r.id, r.companyId, r.authorUserId, r.rating, r.title, r.body,
                    r.pros, r.cons, r.employmentStatus, r.createdAt);
        }
    }
}
