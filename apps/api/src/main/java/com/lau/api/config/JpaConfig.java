package com.lau.api.config;

import com.lau.api.dto.personal.PersonalAuth;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditor")
public class JpaConfig {

    /** R5: autor de creado_por / modificado_por = id del Personal del token. */
    @Bean
    public AuditorAware<UUID> auditor() {
        return JpaConfig::usuarioActual;
    }

    /** Id del Personal autenticado en la petición actual, si lo hay. */
    public static Optional<UUID> usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof PersonalAuth p) ? Optional.of(p.id()) : Optional.empty();
    }
}
