package com.lau.api.repositorio;

import com.lau.api.dominio.AbastecimientoCombustible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AbastecimientoRepositorio extends JpaRepository<AbastecimientoCombustible, UUID>, JpaSpecificationExecutor<AbastecimientoCombustible> {
    Page<AbastecimientoCombustible> findByEmbarcacionId(UUID embarcacionId, Pageable pageable);
}
