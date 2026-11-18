package com.lau.api.repositorio;

import com.lau.api.dominio.Mantenimiento;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MantenimientoRepositorio extends JpaRepository<Mantenimiento, UUID>,
        JpaSpecificationExecutor<Mantenimiento> {

    /** Hoja de vida: intervenciones de una embarcación, la más reciente primero. */
    @EntityGraph(attributePaths = {"motor", "responsable", "registradoPor"})
    List<Mantenimiento> findByEmbarcacionIdOrderByFechaIntervencionDesc(UUID embarcacionId);
}