package co.gov.monteria.businu.mantenimiento;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, UUID> {

    /** Hoja de vida: intervenciones de una embarcación, la más reciente primero. */
    List<Mantenimiento> findByEmbarcacionIdOrderByFechaIntervencionDesc(UUID embarcacionId);
}
