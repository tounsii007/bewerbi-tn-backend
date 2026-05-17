package tn.bewerbi.profile;

import tn.bewerbi.domain.profile.Profile;

/**
 * Single source of truth for profile completeness scoring.
 * Mirrored in Flutter and Web clients — keep weights in sync.
 */
public final class ProfileCompleteness {

    private static final int TOTAL = 100;

    private ProfileCompleteness() {}

    public static int compute(Profile p) {
        int score = 0;
        if (isNotBlank(p.getFirstName())) score += 10;
        if (isNotBlank(p.getLastName())) score += 10;
        if (isNotBlank(p.getPhone())) score += 8;
        if (isNotBlank(p.getCity())) score += 6;
        if (isNotBlank(p.getCountry())) score += 4;
        if (isNotBlank(p.getBio())) score += 12;
        if (isNotBlank(p.getPhotoUrl())) score += 10;
        if (isNotBlank(p.getDesiredProfession())) score += 10;
        if (p.getGermanLevel() != null) score += 15;
        if (p.getRecognitionStatus() != null) score += 5;
        if (p.getSkills() != null && !p.getSkills().isEmpty()) score += 10;
        return Math.min(TOTAL, score);
    }

    private static boolean isNotBlank(String s) { return s != null && !s.isBlank(); }
}
