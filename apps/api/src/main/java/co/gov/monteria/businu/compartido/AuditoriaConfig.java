package co.gov.monteria.businu.compartido;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditor")
public class AuditoriaConfig {

    /** Provisional: cuando se agregue Spring Security, devolverá el username autenticado. */
    @Bean
    AuditorAware<String> auditor() {
        return () -> Optional.of("sistema");
    }
}
