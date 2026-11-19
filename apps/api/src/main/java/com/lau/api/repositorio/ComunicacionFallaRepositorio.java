package com.lau.api.repositorio;

import com.lau.api.dominio.ComunicacionFalla;
import com.lau.api.dominio.enums.comunicacion.TipoReferenciaFalla;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunicacionFallaRepositorio extends JpaRepository<ComunicacionFalla, UUID> {

    @EntityGraph(attributePaths = "registradoPor")
    List<ComunicacionFalla> findByReferenciaTipoAndReferenciaIdOrderByCreadoEnDesc(TipoReferenciaFalla tipo, UUID id);
}
