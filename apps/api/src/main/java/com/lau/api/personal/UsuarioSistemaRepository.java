package com.lau.api.personal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, UUID> {

    Optional<UsuarioSistema> findByUsername(String username);
}
