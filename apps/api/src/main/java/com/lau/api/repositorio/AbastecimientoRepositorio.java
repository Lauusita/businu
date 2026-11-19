package com.lau.api.repositorio;

import java.util.List;

import java.time.Instant;

import com.lau.api.dominio.AbastecimientoCombustible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AbastecimientoRepositorio extends JpaRepository<AbastecimientoCombustible, UUID>, JpaSpecificationExecutor<AbastecimientoCombustible> {
    Page<AbastecimientoCombustible> findByEmbarcacionId(UUID embarcacionId, Pageable pageable);

    /** Cargues aún no asociados a un viaje, hechos entre el viaje anterior y el zarpe actual (R16). */
    List<AbastecimientoCombustible> findByEmbarcacionIdAndOperacionIsNullAndFechaHoraGreaterThanAndFechaHoraLessThanEqualOrderByFechaHora(
            UUID embarcacionId, Instant desde, Instant hasta);

    /** Todos los abastecimientos de la embarcación en orden cronológico (cálculo de consumo, R37). */
    List<AbastecimientoCombustible> findByEmbarcacionIdOrderByFechaHoraAsc(UUID embarcacionId);
}
