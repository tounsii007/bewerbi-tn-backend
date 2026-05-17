package tn.bewerbi.notification;

import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.i18n.MessageClient;
import tn.bewerbi.common.security.JwtSecurityConfig;
import tn.bewerbi.common.security.SecurityFilterChainRegistrar;

/**
 * Queues & delivers notifications. Messages are resolved via the i18n-service
 * using the target user's preferred locale so every outgoing mail is in the
 * right language without the caller needing to know.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"tn.bewerbi.notification", "tn.bewerbi.common.i18n"})
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class, SecurityFilterChainRegistrar.class})
public class NotificationApp {
    public static void main(String[] args) { SpringApplication.run(NotificationApp.class, args); }

    public record EmailRequest(
            @NotBlank String to,
            @NotBlank String subjectKey,
            @NotBlank String bodyKey,
            String locale,
            Object[] args) {}

    public record AlertRequest(
            @NotBlank String to, String locale,
            @NotNull Integer newMatches) {}

    @RestController @RequestMapping("/api/v1/notifications") @PreAuthorize("hasRole('ADMIN')")
    public static class NotificationController {
        private final EmailService svc;
        public NotificationController(EmailService svc) { this.svc = svc; }

        @PostMapping("/email")
        public void email(@RequestBody EmailRequest req) { svc.sendMessage(req); }

        @PostMapping("/alerts/new-matches")
        public void alert(@RequestBody AlertRequest req) { svc.alertNewMatches(req); }
    }

    @Service
    public static class EmailService {
        private final JavaMailSender mailer;
        private final MessageClient messages;
        @Value("${bewerbi.notifications.from}") private String from;

        public EmailService(JavaMailSender mailer, MessageClient messages) {
            this.mailer = mailer; this.messages = messages;
        }

        public void sendMessage(EmailRequest r) {
            String locale = r.locale() == null ? "de" : r.locale();
            String subject = messages.resolveIn(locale, r.subjectKey(), r.args());
            String body = messages.resolveIn(locale, r.bodyKey(), r.args());
            send(r.to(), subject, body);
        }

        public void alertNewMatches(AlertRequest r) {
            String locale = r.locale() == null ? "de" : r.locale();
            String subject = messages.resolveIn(locale, "email.newMatches.subject", r.newMatches());
            String body = messages.resolveIn(locale, "email.newMatches.body",
                    r.newMatches(), "https://bewerbi.tn/search");
            send(r.to(), subject, body);
        }

        private void send(String to, String subject, String body) {
            var msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailer.send(msg);
        }
    }
}
