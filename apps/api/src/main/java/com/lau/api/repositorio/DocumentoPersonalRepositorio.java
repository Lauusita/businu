package com.lau.api.repositorio;

import com.lau.api.dominio.DocumentoPersonal;
import com.lau.api.dominio.enums.personal.TipoDocumentoPersonal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoPersonalRepositorio extends JpaRepository<DocumentoPersonal, UUID> {

    @EntityGraph(attributePaths = {"adjunto", "registradoPor"})
    List<DocumentoPersonal> findByPersonalIdOrderByTipoAscCreadoEnDesc(UUID personalId);

    /** Vigente del mismo tipo (y mes, para los periódicos): es el que se reemplaza. */
    List<DocumentoPersonal> findByPersonalIdAndTipoAndVigenteTrue(UUID personalId, TipoDocumentoPersonal tipo);

    List<DocumentoPersonal> findByPersonalIdAndTipoAndPeriodoAndVigenteTrue(UUID personalId, TipoDocumentoPersonal tipo,
                                                                           LocalDate periodo);

    /** Planillas PILA de un mes (informe mensual: soportes de seguridad social). */
    @EntityGraph(attributePaths = {"personal", "adjunto"})
    List<DocumentoPersonal> findByTipoAndPeriodoAndVigenteTrue(TipoDocumentoPersonal tipo, LocalDate periodo);
}
