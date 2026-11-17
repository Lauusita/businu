package com.lau.api.repositorio;

import com.lau.api.dominio.RegistroInfraestructura;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.EstadoSeguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroInfraestructuraRepositorio
        extends JpaRepository<RegistroInfraestructura, UUID>, JpaSpecificationExecutor<RegistroInfraestructura> {

    /** Último registro: su estadoObservado es el estado actual. */
    Optional<RegistroInfraestructura> findFirstByEmbarcaderoOrderByFechaHoraDesc(Embarcaderos embarcadero);

    Optional<RegistroInfraestructura> findFirstByAyudaIdOrderByFechaHoraDesc(UUID ayudaId);

    long countByEmbarcaderoAndEstadoNot(Embarcaderos embarcadero, EstadoSeguimiento estado);

    long countByAyudaIdAndEstadoNot(UUID ayudaId, EstadoSeguimiento estado);

    /** Requieren a otra entidad y el supervisor aún no fue informado. */
    List<RegistroInfraestructura> findByRequiereIntervencionExternaTrueAndEstadoOrderByFechaHora(
            EstadoSeguimiento estado);
}