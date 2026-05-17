package tn.bewerbi.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.security.JwtSecurityConfig;
import tn.bewerbi.common.security.SecurityFilterChainRegistrar;

@SpringBootApplication
@ComponentScan(basePackages = {"tn.bewerbi.jobs", "tn.bewerbi.common.i18n"})
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class, SecurityFilterChainRegistrar.class})
@EnableJpaAuditing
public class JobsApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobsApplication.class, args);
    }
}
