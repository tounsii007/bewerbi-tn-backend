package tn.bewerbi.domain.profile;

public enum GermanLevel {
    A1, A2, B1, B2, C1, C2;

    /** Returns true if this level is at least as high as the required one. */
    public boolean meetsOrExceeds(GermanLevel required) {
        return required == null || this.ordinal() >= required.ordinal();
    }
}
