package tn.bewerbi.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bewerbi")
public record BewerbiProperties(
        Security security,
        Upload upload,
        Integrations integrations) {

    public record Security(Jwt jwt, Cors cors) {}

    public record Jwt(
            @NotBlank String secret,
            @Positive int accessTtlMinutes,
            @Positive int refreshTtlDays,
            @NotBlank String issuer) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Upload(Path root) {}

    public record Integrations(Anthropic anthropic, Stripe stripe) {}

    public record Anthropic(String apiKey, String model) {}

    public record Stripe(String secretKey, String webhookSecret) {}
}
