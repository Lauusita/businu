package com.lau.api.repositorio;

import com.lau.api.dominio.Usuario;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {
    @Override
    @NullMarked
    @EntityGraph(attributePaths = {"operacion", "operacion.embarcacion", "registradoPor"})
    Page<Usuario> findAll(Specification<Usuario> spec, Pageable pageable);
}
