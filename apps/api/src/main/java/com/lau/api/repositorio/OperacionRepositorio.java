package com.lau.api.repositorio;

import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.enums.EstadoOperacion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperacionRepositorio extends JpaRepository<Operacion, UUID>, JpaSpecificationExecutor<Operacion> {

    boolean existsByEmbarcacionIdAndEstado(UUID embarcacionId, EstadoOperacion estado);

    List<Operacion> findByEstadoOrderByFechaHoraInicio(EstadoOperacion estado);
}