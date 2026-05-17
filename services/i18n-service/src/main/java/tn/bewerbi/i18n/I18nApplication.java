package tn.bewerbi.i18n;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import tn.bewerbi.common.api.GlobalExceptionHandler;
import tn.bewerbi.common.security.JwtSecurityConfig;

@SpringBootApplication
@Import({GlobalExceptionHandler.class, JwtSecurityConfig.class})
@EnableJpaAuditing
@EnableCaching
public class I18nApplication {
    public static void main(String[] args) {
        SpringApplication.run(I18nApplication.class, args);
    }
}
