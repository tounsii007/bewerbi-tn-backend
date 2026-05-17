package tn.bewerbi.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tn.bewerbi.common.events.DomainEvents;
import tn.bewerbi.common.events.Topics;

/**
 * Kafka consumers that react to domain events and dispatch notifications.
 *
 * Each listener resolves message keys through the i18n-service using the
 * recipient's own locale so every mail is delivered in the right language
 * without the caller having to think about translation.
 */
@Component
public class EventListeners {

    private static final Logger log = LoggerFactory.getLogger(EventListeners.class);

    private final NotificationApp.EmailService emails;
    private final ObjectMapper mapper;

    @Value("${bewerbi.notifications.base-url:https://bewerbi.tn}")
    private String frontendBaseUrl;

    public EventListeners(NotificationApp.EmailService emails, ObjectMapper mapper) {
        this.emails = emails;
        this.mapper = mapper;
    }

    /** Sends the welcome / verify-email mail right after registration. */
    @KafkaListener(topics = Topics.USER_REGISTERED)
    public void onUserRegistered(String payload) {
        try {
            var event = mapper.readValue(payload, DomainEvents.UserRegistered.class);
            log.info("UserRegistered: user={} locale={}", event.userId(), event.preferredLocale());

            // The identity-service includes the single-use verification token
            // inside the event — embed it directly so the recipient can click
            // through to GET /api/v1/auth/verify-email?token=...
            String verifyLink = "%s/verify?token=%s&lang=%s".formatted(
                    frontendBaseUrl,
                    event.verificationToken(),
                    event.preferredLocale());

            emails.sendMessage(new NotificationApp.EmailRequest(
                    event.email(),
                    "email.verify.subject",
                    "email.verify.body",
                    event.preferredLocale(),
                    new Object[]{event.firstName(), verifyLink}));
        } catch (Exception e) {
            log.warn("Failed to process UserRegistered: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = Topics.APPLICATION_CREATED)
    public void onApplicationCreated(String payload) {
        try {
            var event = mapper.readValue(payload, DomainEvents.ApplicationCreated.class);
            log.info("ApplicationCreated: application={} job={} applicant={}",
                    event.applicationId(), event.jobId(), event.applicantUserId());
            // Next step: fetch applicant profile via identity-service for the e-mail
            // address + locale, then call emails.sendMessage(...) with
            // "email.applied.subject" / "email.applied.body". Kept minimal here
            // to avoid a synchronous cross-service call in a Kafka listener —
            // scheduled batch approach is planned.
        } catch (Exception e) {
            log.warn("Failed to process ApplicationCreated: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = Topics.COMPANY_VERIFIED)
    public void onCompanyVerified(String payload) {
        try {
            var event = mapper.readValue(payload, DomainEvents.CompanyVerified.class);
            log.info("CompanyVerified: {} ({})", event.name(), event.slug());
        } catch (Exception e) {
            log.warn("Failed to process CompanyVerified: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = Topics.JOB_PUBLISHED)
    public void onJobPublished(String payload) {
        try {
            var event = mapper.readValue(payload, DomainEvents.JobPublished.class);
            log.info("JobPublished: {} in {} ({})", event.title(), event.location(), event.category());
            // Hook for saved-search alerts: query subscribers whose saved
            // searches match this job and trigger their configured alert
            // frequency (INSTANT fires now, DAILY/WEEKLY are aggregated by
            // the alert scheduler).
        } catch (Exception e) {
            log.warn("Failed to process JobPublished: {}", e.getMessage());
        }
    }
}
