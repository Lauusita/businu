package com.lau.api.repositorio;

import com.lau.api.dominio.TokenRecuperacion;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRecuperacionRepositorio extends JpaRepository<TokenRecuperacion, UUID> {

    /** Bloquea la fila: dos peticiones con el mismo enlace no pueden usarlo a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    boolean existsByPersonal_IdAndCreadoEnAfter(UUID personalId, Instant desde);

    /** Anula los enlaces pendientes de la persona (al pedir uno nuevo o al usar uno). */
    @Modifying(flushAutomatically = true)
    @Query("update TokenRecuperacion t set t.usadoEn = :ahora where t.personal.id = :personalId and t.usadoEn is null")
    int anularPendientes(@Param("personalId") UUID personalId, @Param("ahora") Instant ahora);
}
