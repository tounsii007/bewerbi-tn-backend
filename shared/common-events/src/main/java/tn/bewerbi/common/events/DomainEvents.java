package tn.bewerbi.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Event envelopes published on Kafka. Each one is an immutable record so
 * services can safely share the type without accidental mutation.
 */
public final class DomainEvents {
    private DomainEvents() {}

    public record UserRegistered(
            UUID userId, String email, String firstName, String role,
            String preferredLocale,
            /** One-time token the recipient uses to activate the account. */
            String verificationToken,
            Instant occurredAt) {}

    public record ApplicationCreated(
            UUID applicationId, UUID jobId, UUID applicantUserId,
            Instant occurredAt) {}

    public record CompanyVerified(
            UUID companyId, String name, String slug, Instant occurredAt) {}

    public record JobPublished(
            UUID jobId, UUID companyId, String title, String category, String type,
            String location, Instant occurredAt) {}
}
