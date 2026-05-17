package tn.bewerbi.domain.anerkennung;

public enum RegulationType {
    /** Reglementierter Beruf (z. B. Arzt, Krankenpfleger) — Anerkennung zwingend. */
    REGULATED,
    /** Nicht reglementiert (z. B. IT, kaufmännisch) — Anerkennung freiwillig. */
    NON_REGULATED,
    UNKNOWN
}
