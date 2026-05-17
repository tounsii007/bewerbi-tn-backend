package tn.bewerbi.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.api.ResourceNotFoundException;
import tn.bewerbi.domain.profile.GermanLevel;
import tn.bewerbi.domain.profile.Profile;
import tn.bewerbi.domain.profile.ProfileRepository;
import tn.bewerbi.domain.profile.RecognitionStatus;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping("/me")
    @Operation(summary = "Get the current user's profile + computed completeness score")
    public ProfileResponse me() {
        return service.me(CurrentUser.id());
    }

    @PutMapping("/me")
    public ProfileResponse update(@Valid @RequestBody ProfileUpdateRequest req) {
        return service.update(CurrentUser.id(), req);
    }

    @PostMapping("/onboarding")
    @Operation(summary = "Complete onboarding quiz (profession, German level, recognition status)")
    public ProfileResponse completeOnboarding(@Valid @RequestBody OnboardingRequest req) {
        return service.completeOnboarding(CurrentUser.id(), req);
    }

    public record ProfileResponse(
            UUID id, UUID userId, String firstName, String lastName, String phone,
            String city, String country, String bio, String photoUrl,
            String desiredProfession, GermanLevel germanLevel, RecognitionStatus recognitionStatus,
            boolean onboardingCompleted, List<String> skills, int completenessPercent) {}

    public record ProfileUpdateRequest(
            String firstName, String lastName, String phone, String city, String country,
            String bio, String photoUrl, String desiredProfession,
            GermanLevel germanLevel, RecognitionStatus recognitionStatus, List<String> skills) {}

    public record OnboardingRequest(
            String desiredProfession, GermanLevel germanLevel,
            RecognitionStatus recognitionStatus, List<String> skills) {}

    @Service
    @Transactional
    public static class ProfileService {

        private final ProfileRepository profiles;

        public ProfileService(ProfileRepository profiles) { this.profiles = profiles; }

        public ProfileResponse me(UUID userId) {
            return toResponse(profileFor(userId));
        }

        public ProfileResponse update(UUID userId, ProfileUpdateRequest r) {
            var p = profileFor(userId);
            if (r.firstName() != null) p.setFirstName(r.firstName());
            if (r.lastName() != null) p.setLastName(r.lastName());
            if (r.phone() != null) p.setPhone(r.phone());
            if (r.city() != null) p.setCity(r.city());
            if (r.country() != null) p.setCountry(r.country());
            if (r.bio() != null) p.setBio(r.bio());
            if (r.photoUrl() != null) p.setPhotoUrl(r.photoUrl());
            if (r.desiredProfession() != null) p.setDesiredProfession(r.desiredProfession());
            if (r.germanLevel() != null) p.setGermanLevel(r.germanLevel());
            if (r.recognitionStatus() != null) p.setRecognitionStatus(r.recognitionStatus());
            if (r.skills() != null) { p.getSkills().clear(); p.getSkills().addAll(r.skills()); }
            return toResponse(p);
        }

        public ProfileResponse completeOnboarding(UUID userId, OnboardingRequest r) {
            var p = profileFor(userId);
            if (r.desiredProfession() != null) p.setDesiredProfession(r.desiredProfession());
            if (r.germanLevel() != null) p.setGermanLevel(r.germanLevel());
            if (r.recognitionStatus() != null) p.setRecognitionStatus(r.recognitionStatus());
            if (r.skills() != null) { p.getSkills().clear(); p.getSkills().addAll(r.skills()); }
            p.setOnboardingCompleted(true);
            return toResponse(p);
        }

        private Profile profileFor(UUID userId) {
            return profiles.findByUserId(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Profile", userId));
        }

        private ProfileResponse toResponse(Profile p) {
            return new ProfileResponse(
                    p.getId(), p.getUserId(), p.getFirstName(), p.getLastName(), p.getPhone(),
                    p.getCity(), p.getCountry(), p.getBio(), p.getPhotoUrl(),
                    p.getDesiredProfession(), p.getGermanLevel(), p.getRecognitionStatus(),
                    p.isOnboardingCompleted(), p.getSkills(),
                    ProfileCompleteness.compute(p));
        }
    }
}
