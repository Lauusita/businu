package com.lau.api.repositories;

import com.lau.api.domain.RegistroPasajeros;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroPasajerosRepository extends JpaRepository<RegistroPasajeros, UUID> {

    List<RegistroPasajeros> findByFechaHoraBetweenOrderByFechaHora(Instant desde, Instant hasta);
}
