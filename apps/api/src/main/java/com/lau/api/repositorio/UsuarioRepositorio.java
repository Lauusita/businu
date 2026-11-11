package com.lau.api.repositorio;

import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.enums.Embarcaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public abstract class UsuarioRepositorio implements JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {
    public abstract List<Usuario> findUsuarioByEmbarcaciones(Embarcaciones embarcacion);
    public abstract List<Usuario> findUsuarioByFechaActualizacion(LocalDateTime fecha);


}
