package com.lau.api.repositorio;

import com.lau.api.dominio.SuspensionEmbarcacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspensionEmbarcacionRepositorio extends JpaRepository<SuspensionEmbarcacion, UUID> {

    Optional<SuspensionEmbarcacion> findFirstByEmbarcacionIdAndFechaFinIsNull(UUID embarcacionId);

    /** Historial completo, la más reciente primero. */
    @EntityGraph(attributePaths = {"embarcacion", "operacion", "registradoPor", "levantadaPor"})
    List<SuspensionEmbarcacion> findByEmbarcacionIdOrderByFechaInicioDesc(UUID embarcacionId);
}
