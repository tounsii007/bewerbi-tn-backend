package tn.bewerbi.identity.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.CurrentUser;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.common.i18n.LocaleContext;
import tn.bewerbi.common.i18n.MessageClient;
import tn.bewerbi.identity.domain.*;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService service;
    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping("/me")
    @Operation(summary = "Current user's profile + localized completeness")
    public ProfileResponse me() { return service.me(CurrentUser.id()); }

    @PutMapping("/me")
    public ProfileResponse update(@Valid @RequestBody ProfileUpdateRequest req) {
        return service.update(CurrentUser.id(), req);
    }

    @PostMapping("/onboarding")
    public ProfileResponse completeOnboarding(@Valid @RequestBody OnboardingRequest req) {
        return service.completeOnboarding(CurrentUser.id(), req);
    }

    @PutMapping("/me/locale")
    @Operation(summary = "Persist the user's preferred locale on the server")
    public void updateLocale(@RequestBody LocaleRequest req) {
        service.updateLocale(CurrentUser.id(), req.locale());
    }

    public record ProfileResponse(
            UUID id, UUID userId, String firstName, String lastName, String phone,
            String city, String country, String bio, String photoUrl,
            String desiredProfession, GermanLevel germanLevel, RecognitionStatus recognitionStatus,
            boolean onboardingCompleted, List<String> skills,
            CompletenessResponse completeness) {}

    public record CompletenessResponse(
            int percent, String tier, String tierLabel,
            List<LocalizedMissingField> missing, LocalizedMissingField nextAction) {}

    public record LocalizedMissingField(
            String key, int weight, String label, String action, String route) {}

    public record ProfileUpdateRequest(
            String firstName, String lastName, String phone, String city, String country,
            String bio, String photoUrl, String desiredProfession,
            GermanLevel germanLevel, RecognitionStatus recognitionStatus, List<String> skills) {}

    public record OnboardingRequest(
            String desiredProfession, GermanLevel germanLevel,
            RecognitionStatus recognitionStatus, List<String> skills) {}

    public record LocaleRequest(String locale) {}

    @Service
    @Transactional
    public static class ProfileService {

        private final ProfileRepository profiles;
        private final UserRepository users;
        private final MessageClient messages;

        public ProfileService(ProfileRepository profiles, UserRepository users, MessageClient messages) {
            this.profiles = profiles; this.users = users; this.messages = messages;
        }

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

        public void updateLocale(UUID userId, String locale) {
            users.findById(userId).ifPresent(u -> u.setPreferredLocale(locale));
        }

        private Profile profileFor(UUID userId) {
            return profiles.findByUserId(userId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Profile", userId));
        }

        private ProfileResponse toResponse(Profile p) {
            var result = ProfileCompleteness.compute(p);
            var missingLocalized = result.missing().stream()
                    .map(this::localize)
                    .toList();
            var nextLocalized = result.nextAction() == null ? null : localize(result.nextAction());
            var tierLabel = messages.resolve("tier." + result.tier().name().toLowerCase());
            var completeness = new CompletenessResponse(
                    result.percent(), result.tier().name(), tierLabel,
                    missingLocalized, nextLocalized);
            return new ProfileResponse(
                    p.getId(), p.getUserId(), p.getFirstName(), p.getLastName(), p.getPhone(),
                    p.getCity(), p.getCountry(), p.getBio(), p.getPhotoUrl(),
                    p.getDesiredProfession(), p.getGermanLevel(), p.getRecognitionStatus(),
                    p.isOnboardingCompleted(), p.getSkills(), completeness);
        }

        private LocalizedMissingField localize(ProfileCompleteness.MissingField f) {
            return new LocalizedMissingField(
                    f.key(), f.weight(),
                    messages.resolve(f.labelKey()),
                    messages.resolve(f.actionKey()),
                    f.route());
        }
    }
}
