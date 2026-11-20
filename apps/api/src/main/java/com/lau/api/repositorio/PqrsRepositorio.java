package com.lau.api.repositorio;

import com.lau.api.dominio.Pqrs;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PqrsRepositorio extends JpaRepository<Pqrs, UUID>, JpaSpecificationExecutor<Pqrs> {

    /** Consecutivo del radicado (secuencia de la base: no se repite aunque lleguen dos a la vez). */
    @Query(value = "SELECT nextval('pqrs_consecutivo_seq')", nativeQuery = true)
    long siguienteConsecutivo();
}
