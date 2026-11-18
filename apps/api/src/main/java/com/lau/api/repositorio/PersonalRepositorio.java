package com.lau.api.repositorio;

import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalRepositorio extends JpaRepository<Personal, UUID>, JpaSpecificationExecutor<Personal> {
    Optional<Personal> findByNumeroIdentificacion(String identificacion);
    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    boolean existsByEmail(String email);

    List<Personal> findByRolIn(Collection<Rol> roles);
}
