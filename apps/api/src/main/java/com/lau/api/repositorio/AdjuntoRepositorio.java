package com.lau.api.repositorio;

import com.lau.api.dominio.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AdjuntoRepositorio extends JpaRepository<Adjunto, UUID>, JpaSpecificationExecutor<Adjunto> {
}
