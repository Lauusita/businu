package com.lau.api.mantenimiento;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MantenimientoMotorRepository extends JpaRepository<MantenimientoMotor, UUID> {

    /** Último servicio periódico (100 h) del motor: es el que fija el próximo horómetro. */
    @EntityGraph(attributePaths = "mantenimiento")
    Optional<MantenimientoMotor> findFirstByMotorIdAndServicioPeriodicoTrueOrderByHorometroDesc(UUID motorId);
}
