package com.lau.api.repositorio;

import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperacionRepositorio extends JpaRepository<Operacion, UUID>, JpaSpecificationExecutor<Operacion> {

    boolean existsByEmbarcacionIdAndEstado(UUID embarcacionId, EstadoOperacion estado);

    List<Operacion> findByEstadoOrderByFechaHoraInicio(EstadoOperacion estado);

    boolean existsByEstadoAndTripulacion_IdIn(EstadoOperacion estado, Collection<UUID> personalIds);

    /** Último viaje cerrado de la embarcación antes de un momento (cargues de combustible, R16). */
    Optional<Operacion> findFirstByEmbarcacionIdAndFechaHoraFinLessThanEqualOrderByFechaHoraFinDesc(
            UUID embarcacionId, Instant hasta);

    /** Viajes cerrados de la embarcación en un periodo (consumo entre tanques llenos, R37). */
    List<Operacion> findByEmbarcacionIdAndEstadoInAndFechaHoraInicioGreaterThanAndFechaHoraInicioLessThan(
            UUID embarcacionId, Collection<EstadoOperacion> estados, Instant desde, Instant hasta);
}