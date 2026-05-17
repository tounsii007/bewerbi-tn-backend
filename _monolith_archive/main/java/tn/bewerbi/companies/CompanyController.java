package tn.bewerbi.companies;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.ConflictException;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.company.Company;
import tn.bewerbi.domain.company.CompanyRepository;
import tn.bewerbi.domain.company.VerificationStatus;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) { this.service = service; }

    @GetMapping
    public Page<CompanyResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{slug}")
    public CompanyResponse getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create a company profile (owner = current user)")
    public CompanyResponse create(@Valid @RequestBody CompanyCreateRequest req) {
        return service.create(CurrentUser.id(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public CompanyResponse update(@PathVariable UUID id, @RequestBody CompanyUpdateRequest req) {
        return service.update(CurrentUser.id(), id, req);
    }

    @PostMapping("/{id}/verification-request")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Submit verification evidence; admin will review")
    public CompanyResponse requestVerification(
            @PathVariable UUID id,
            @RequestBody VerificationRequest req) {
        return service.requestVerification(CurrentUser.id(), id, req);
    }

    @PostMapping("/{id}/verification-decision")
    @PreAuthorize("hasRole('ADMIN')")
    public CompanyResponse decideVerification(
            @PathVariable UUID id,
            @RequestBody VerificationDecision decision) {
        return service.decide(id, decision);
    }

    public record CompanyResponse(
            UUID id, String name, String slug, String description, String website, String logoUrl,
            String industry, String size, String country, String city,
            VerificationStatus verificationStatus, Double ratingAvg, int ratingCount) {}

    public record CompanyCreateRequest(
            @NotBlank String name, @NotBlank String slug, String description, String website,
            String logoUrl, String industry, String size, String country, String city) {}

    public record CompanyUpdateRequest(
            String name, String description, String website, String logoUrl,
            String industry, String size, String country, String city) {}

    public record VerificationRequest(String tradeRegisterNumber, String note) {}

    public record VerificationDecision(VerificationStatus status, String note) {}

    @Service
    @Transactional
    public static class CompanyService {

        private final CompanyRepository repo;

        public CompanyService(CompanyRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public Page<CompanyResponse> list(Pageable pageable) {
            return repo.findAll(pageable).map(this::toResponse);
        }

        @Transactional(readOnly = true)
        public CompanyResponse getBySlug(String slug) {
            return repo.findBySlug(slug)
                    .map(this::toResponse)
                    .orElseThrow(() -> ResourceNotFoundException.of("Company", slug));
        }

        public CompanyResponse create(UUID ownerId, CompanyCreateRequest r) {
            if (repo.existsBySlug(r.slug())) {
                throw new ConflictException("Slug already taken");
            }
            var c = new Company(ownerId, r.name(), r.slug());
            c.setDescription(r.description());
            c.setWebsite(r.website());
            c.setLogoUrl(r.logoUrl());
            c.setIndustry(r.industry());
            c.setSize(r.size());
            c.setCountry(r.country());
            c.setCity(r.city());
            return toResponse(repo.save(c));
        }

        public CompanyResponse update(UUID ownerId, UUID id, CompanyUpdateRequest r) {
            var c = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Company", id));
            if (!c.getOwnerUserId().equals(ownerId)) throw new ConflictException("Not your company");
            if (r.name() != null) c.setName(r.name());
            if (r.description() != null) c.setDescription(r.description());
            if (r.website() != null) c.setWebsite(r.website());
            if (r.logoUrl() != null) c.setLogoUrl(r.logoUrl());
            if (r.industry() != null) c.setIndustry(r.industry());
            if (r.size() != null) c.setSize(r.size());
            if (r.country() != null) c.setCountry(r.country());
            if (r.city() != null) c.setCity(r.city());
            return toResponse(c);
        }

        public CompanyResponse requestVerification(UUID ownerId, UUID id, VerificationRequest r) {
            var c = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Company", id));
            if (!c.getOwnerUserId().equals(ownerId)) throw new ConflictException("Not your company");
            if (r.tradeRegisterNumber() != null) c.setTradeRegisterNumber(r.tradeRegisterNumber());
            c.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
            c.setVerificationNote(r.note());
            return toResponse(c);
        }

        public CompanyResponse decide(UUID id, VerificationDecision d) {
            var c = repo.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Company", id));
            c.setVerificationStatus(d.status());
            c.setVerificationNote(d.note());
            return toResponse(c);
        }

        private CompanyResponse toResponse(Company c) {
            return new CompanyResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(),
                    c.getWebsite(), c.getLogoUrl(), c.getIndustry(), c.getSize(),
                    c.getCountry(), c.getCity(), c.getVerificationStatus(),
                    c.getRatingAvg(), c.getRatingCount());
        }
    }
}
