package tn.bewerbi.domain.anerkennung;

public enum AnerkennungStage {
    INFORMATION,           // 1. Information einholen
    DOCUMENTS_COLLECTION,  // 2. Unterlagen sammeln (Zeugnisse, Übersetzungen)
    APPLICATION_SUBMITTED, // 3. Antrag eingereicht
    EQUIVALENCE_REVIEW,    // 4. Gleichwertigkeitsprüfung läuft
    COMPENSATION_REQUIRED, // 5. Ausgleichsmaßnahme nötig (Kenntnis-/Anpassungsprüfung)
    COMPLETED              // 6. Anerkennungsbescheid erhalten
}
