package tn.bewerbi.domain.visa;

public enum VisaType {
    /** Blaue Karte EU — §18b AufenthG. */
    BLUE_CARD,
    /** Fachkraft mit Berufsausbildung — §18a AufenthG. */
    SKILLED_WORKER_VOCATIONAL,
    /** Fachkraft mit akademischer Ausbildung — §18b AufenthG. */
    SKILLED_WORKER_ACADEMIC,
    /** Zur Ausbildung — §16a AufenthG. */
    VOCATIONAL_TRAINING,
    /** Zum Studium — §16b AufenthG. */
    STUDY,
    /** Arbeitsplatzsuche — §20 AufenthG. */
    JOB_SEEKER,
    /** Anerkennung der Berufsqualifikation — §16d AufenthG. */
    RECOGNITION,
    /** Chancenkarte — §20a AufenthG (seit 06/2024). */
    CHANCENKARTE
}
