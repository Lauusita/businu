package com.lau.api.repositories;

import com.lau.api.domain.Usuario;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    List<Usuario> findByFechaHoraBetweenOrderByFechaHora(Instant desde, Instant hasta);
}
