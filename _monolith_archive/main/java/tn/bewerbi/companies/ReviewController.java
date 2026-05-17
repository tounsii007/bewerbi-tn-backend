package tn.bewerbi.companies;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.company.CompanyRepository;
import tn.bewerbi.domain.company.Review;
import tn.bewerbi.domain.company.ReviewRepository;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/reviews")
@Tag(name = "Company Reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) { this.service = service; }

    @GetMapping
    public Page<ReviewResponse> list(@PathVariable UUID companyId, Pageable pageable) {
        return service.list(companyId, pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ReviewResponse create(@PathVariable UUID companyId,
                                 @Valid @RequestBody ReviewRequest req) {
        return service.create(CurrentUser.id(), companyId, req);
    }

    public record ReviewRequest(
            @Min(1) @Max(5) int rating,
            String title, String body, String pros, String cons, String employmentStatus) {}

    public record ReviewResponse(
            UUID id, UUID companyId, UUID authorUserId, int rating,
            String title, String body, String pros, String cons,
            String employmentStatus, Instant createdAt) {}

    @Service
    @Transactional
    public static class ReviewService {

        private final ReviewRepository reviews;
        private final CompanyRepository companies;

        public ReviewService(ReviewRepository reviews, CompanyRepository companies) {
            this.reviews = reviews;
            this.companies = companies;
        }

        @Transactional(readOnly = true)
        public Page<ReviewResponse> list(UUID companyId, Pageable pageable) {
            return reviews.findByCompanyId(companyId, pageable).map(this::toResponse);
        }

        public ReviewResponse create(UUID authorUserId, UUID companyId, ReviewRequest r) {
            var company = companies.findById(companyId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Company", companyId));

            var review = new Review(companyId, authorUserId, r.rating());
            review.setTitle(r.title());
            review.setBody(r.body());
            review.setPros(r.pros());
            review.setCons(r.cons());
            review.setEmploymentStatus(r.employmentStatus());
            reviews.save(review);

            // Re-aggregate rating
            Object[] agg = reviews.aggregateByCompany(companyId);
            double avg = agg[0] instanceof Number n ? n.doubleValue() : 0.0;
            int count = agg[1] instanceof Number n ? n.intValue() : 0;
            company.updateRating(avg, count);

            return toResponse(review);
        }

        private ReviewResponse toResponse(Review r) {
            return new ReviewResponse(r.getId(), r.getCompanyId(), r.getAuthorUserId(),
                    r.getRating(), r.getTitle(), r.getBody(), r.getPros(), r.getCons(),
                    r.getEmploymentStatus(), r.getCreatedAt());
        }
    }
}
