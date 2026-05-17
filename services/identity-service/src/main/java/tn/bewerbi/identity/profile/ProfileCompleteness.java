package tn.bewerbi.identity.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import tn.bewerbi.identity.domain.Profile;

/**
 * Source of truth for profile-completeness scoring, mirrored by the Flutter &
 * Web clients. Returns both the overall percent and the highest-value missing
 * field so the UI can prompt the user with the most impactful next action.
 */
public final class ProfileCompleteness {

    private ProfileCompleteness() {}

    public record MissingField(String key, int weight, String labelKey, String actionKey, String route) {}

    public record Result(int percent, Tier tier, List<MissingField> missing, MissingField nextAction) {}

    public enum Tier {
        STARTER, MOVER, ADVANCED, COMPLETE;
        public static Tier of(int percent) {
            if (percent >= 100) return COMPLETE;
            if (percent >= 75) return ADVANCED;
            if (percent >= 40) return MOVER;
            return STARTER;
        }
    }

    public static Result compute(Profile p) {
        var checks = List.of(
                new Check("firstName", 10, "profile.firstName", "profileAction.addFirstName", "/profile/edit", notBlank(p.getFirstName())),
                new Check("lastName", 10, "profile.lastName", "profileAction.addLastName", "/profile/edit", notBlank(p.getLastName())),
                new Check("phone", 8, "profile.phone", "profileAction.addPhone", "/profile/edit", notBlank(p.getPhone())),
                new Check("city", 6, "profile.city", "profileAction.addCity", "/profile/edit", notBlank(p.getCity())),
                new Check("country", 4, "profile.country", "profileAction.addCountry", "/profile/edit", notBlank(p.getCountry())),
                new Check("bio", 12, "profile.bio", "profileAction.addBio", "/profile/edit", notBlank(p.getBio())),
                new Check("photo", 10, "profile.photo", "profileAction.addPhoto", "/profile/edit", notBlank(p.getPhotoUrl())),
                new Check("desiredProfession", 10, "onboarding.step1Title", "profileAction.addProfession", "/onboarding", notBlank(p.getDesiredProfession())),
                new Check("germanLevel", 15, "profile.level", "profileAction.addGerman", "/onboarding", p.getGermanLevel() != null),
                new Check("recognitionStatus", 5, "anerkennung.title", "profileAction.addRecognition", "/anerkennung", p.getRecognitionStatus() != null),
                new Check("skills", 10, "onboarding.step4Title", "profileAction.addSkills", "/onboarding", p.getSkills() != null && !p.getSkills().isEmpty())
        );

        int percent = 0;
        var missing = new ArrayList<MissingField>();
        for (Check c : checks) {
            if (c.has) {
                percent += c.weight;
            } else {
                missing.add(new MissingField(c.key, c.weight, c.labelKey, c.actionKey, c.route));
            }
        }
        percent = Math.min(100, percent);
        MissingField next = missing.stream()
                .max(Comparator.comparingInt(MissingField::weight))
                .orElse(null);
        return new Result(percent, Tier.of(percent), missing, next);
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private record Check(String key, int weight, String labelKey, String actionKey, String route, boolean has) {}
}
