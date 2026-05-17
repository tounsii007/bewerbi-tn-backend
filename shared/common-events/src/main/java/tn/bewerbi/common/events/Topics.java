package tn.bewerbi.common.events;

/** Canonical list of Kafka topics used across the platform. */
public final class Topics {
    private Topics() {}

    /** Emitted by applications-service when a user applies to a job. */
    public static final String APPLICATION_CREATED = "bewerbi.applications.created";

    /** Emitted by companies-service when an admin approves a verification request. */
    public static final String COMPANY_VERIFIED = "bewerbi.companies.verified";

    /** Emitted by jobs-service when a new job passes moderation. */
    public static final String JOB_PUBLISHED = "bewerbi.jobs.published";

    /** Emitted by identity-service after a successful registration. */
    public static final String USER_REGISTERED = "bewerbi.users.registered";
}
