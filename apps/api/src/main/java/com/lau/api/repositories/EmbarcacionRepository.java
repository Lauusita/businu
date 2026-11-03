package com.lau.api.repositories;

import com.lau.api.domain.Embarcacion;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbarcacionRepository extends JpaRepository<Embarcacion, UUID> {

    Optional<Embarcacion> findByPatente(String patente);
}
