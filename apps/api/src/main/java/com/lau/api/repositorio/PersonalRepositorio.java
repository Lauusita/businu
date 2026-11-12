package com.lau.api.repositorio;

import com.lau.api.dominio.Personal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalRepositorio extends JpaRepository<Personal, UUID>, JpaSpecificationExecutor<Personal> {
    Optional<Personal> findByNumeroIdentificacion(String identificacion);
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    boolean existsByEmail(String email);
}
