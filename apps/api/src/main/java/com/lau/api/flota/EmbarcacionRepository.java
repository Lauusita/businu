package com.lau.api.flota;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbarcacionRepository extends JpaRepository<Embarcacion, UUID> {

    Optional<Embarcacion> findByPatente(String patente);
}
