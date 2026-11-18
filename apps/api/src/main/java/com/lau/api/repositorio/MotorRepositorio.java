package com.lau.api.repositorio;

import com.lau.api.dominio.Motor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;

public interface MotorRepositorio extends JpaRepository<Motor, UUID> {
    Motor findByEmbarcacionId(UUID embarcacionId);
    @EntityGraph(attributePaths = "embarcacion")
    List<Motor> findAllByOrderBySerialAsc();

    @EntityGraph(attributePaths = "embarcacion")
    List<Motor> findByEmbarcacionIdOrderBySerialAsc(UUID embarcacionId);

    boolean existsBySerial(String serial);

    long countByEmbarcacionId(UUID embarcacionId);
}
