package tn.bewerbi.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.security.JwtSecurityConfig;
import tn.bewerbi.common.security.SecurityFilterChainRegistrar;

@SpringBootApplication
@ComponentScan(basePackages = {
        "tn.bewerbi.identity", "tn.bewerbi.common.i18n", "tn.bewerbi.common.events"
})
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class, SecurityFilterChainRegistrar.class})
@EnableJpaAuditing
@EnableAsync
public class IdentityApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
