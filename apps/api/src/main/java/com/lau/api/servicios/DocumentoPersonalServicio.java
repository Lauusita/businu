package com.lau.api.servicios;

import com.lau.api.dominio.Adjunto;
import com.lau.api.dominio.DocumentoPersonal;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.personal.TipoDocumentoPersonal;
import com.lau.api.dto.documento.DocumentacionPersonaResponse;
import com.lau.api.dto.documento.DocumentoPersonalResponse;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.DocumentoPersonalRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Documentación del personal (§4.6). Los archivos van a S3 en documentacion/{ROL}/{cédula}/{TIPO}/. */
@Service
@Transactional(readOnly = true)
public class DocumentoPersonalServicio {

    private final DocumentoPersonalRepositorio documentos;
    private final PersonalRepositorio personal;
    private final AdjuntoServicio adjuntos;

    public DocumentoPersonalServicio(DocumentoPersonalRepositorio documentos, PersonalRepositorio personal,
                                     AdjuntoServicio adjuntos) {
        this.documentos = documentos;
        this.personal = personal;
        this.adjuntos = adjuntos;
    }

    /** PDF o foto (se convierte a PDF). Si ya hay uno vigente del mismo tipo (y mes), pasa al historial. */
    @Transactional
    public DocumentoPersonalResponse registrar(UUID personalId, TipoDocumentoPersonal tipo, YearMonth periodo,
                                               String observaciones, MultipartFile archivo, UUID usuarioId) {
        Personal persona = buscar(personalId);
        Personal quien = buscar(usuarioId);
        String carpeta = "documentacion/" + persona.getRol() + "/" + persona.getNumeroIdentificacion() + "/" + tipo;
        Adjunto adjunto = adjuntos.guardarPdf(carpeta, archivo, usuarioId);
        adjuntos.vincular(adjunto.getId(), quien);

        DocumentoPersonal nuevo = new DocumentoPersonal(persona, tipo, periodo, adjunto, observaciones, quien);
        (tipo.isPorPeriodo()
                ? documentos.findByPersonalIdAndTipoAndPeriodoAndVigenteTrue(personalId, tipo, periodo.atDay(1))
                : documentos.findByPersonalIdAndTipoAndVigenteTrue(personalId, tipo))
                .forEach(DocumentoPersonal::reemplazar);
        return toResponse(documentos.save(nuevo));
    }

    public DocumentacionPersonaResponse documentacion(UUID personalId, boolean incluirHistorial) {
        Personal persona = buscar(personalId);
        List<DocumentoPersonal> lista = documentos.findByPersonalIdOrderByTipoAscCreadoEnDesc(personalId);
        Set<TipoDocumentoPersonal> faltantes = EnumSet.noneOf(TipoDocumentoPersonal.class);
        faltantes.addAll(TipoDocumentoPersonal.requeridosPara(persona.getRol()));
        lista.stream().filter(DocumentoPersonal::isVigente).forEach(d -> faltantes.remove(d.getTipo()));
        List<YearMonth> pila = lista.stream()
                .filter(d -> d.isVigente() && d.getTipo() == TipoDocumentoPersonal.PLANILLA_PILA)
                .map(d -> YearMonth.from(d.getPeriodo()))
                .sorted()
                .toList();
        return new DocumentacionPersonaResponse(persona.getId(), persona.nombreCompleto(),
                persona.getNumeroIdentificacion(), persona.getRol(), faltantes, pila,
                lista.stream().filter(d -> incluirHistorial || d.isVigente()).map(this::toResponse).toList());
    }

    /** Enlace temporal (5 min) para ver el PDF. */
    public String urlDescarga(UUID personalId, UUID documentoId) {
        DocumentoPersonal d = documentos.findById(documentoId)
                .filter(x -> x.getPersonal().getId().equals(personalId))
                .orElseThrow(() -> new NotFoundException("Documento no encontrado para esta persona"));
        return adjuntos.urlDescarga(d.getAdjunto().getId());
    }

    private DocumentoPersonalResponse toResponse(DocumentoPersonal d) {
        Adjunto a = d.getAdjunto();
        return new DocumentoPersonalResponse(d.getId(), d.getTipo(), d.getTipo().getDescripcion(),
                d.getPeriodo() == null ? null : YearMonth.from(d.getPeriodo()), d.isVigente(),
                a.getNombreOriginal(), a.getTamanoBytes(), a.getSha256(), d.getObservaciones(),
                d.getCreadoEn(), d.getRegistradoPor().nombreCompleto());
    }

    private Personal buscar(UUID id) {
        return personal.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }
}
