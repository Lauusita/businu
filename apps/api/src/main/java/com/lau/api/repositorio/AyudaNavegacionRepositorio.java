package com.lau.api.repositorio;

import com.lau.api.dominio.AyudaNavegacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AyudaNavegacionRepositorio extends JpaRepository<AyudaNavegacion, UUID>, JpaSpecificationExecutor<AyudaNavegacion> {

    boolean existsByCodigo(String codigo);

    List<AyudaNavegacion> findByActivaTrueOrderByCodigo();
}