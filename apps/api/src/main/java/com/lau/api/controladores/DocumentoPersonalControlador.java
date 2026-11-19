package com.lau.api.controladores;

import com.lau.api.dominio.enums.personal.TipoDocumentoPersonal;
import com.lau.api.dto.documento.DocumentacionPersonaResponse;
import com.lau.api.dto.documento.DocumentoPersonalResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.DocumentoPersonalServicio;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Documentación del personal (§4.6). Solo el supervisor la carga y la consulta. */
@RestController
@RequestMapping("/personal/{personalId}/documentos")
@PreAuthorize("hasRole('SUPERVISOR')")
public class DocumentoPersonalControlador {

    private final DocumentoPersonalServicio servicio;

    public DocumentoPersonalControlador(DocumentoPersonalServicio servicio) {
        this.servicio = servicio;
    }

    /** multipart: archivo (PDF, JPG o PNG), tipo, periodo (yyyy-MM, solo PILA) y observaciones. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoPersonalResponse> registrar(
            @PathVariable UUID personalId,
            @RequestParam TipoDocumentoPersonal tipo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth periodo,
            @RequestParam(required = false) String observaciones,
            @RequestPart("archivo") MultipartFile archivo,
            @AuthenticationPrincipal PersonalAuth auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.registrar(personalId, tipo, periodo, observaciones, archivo, auth.id()));
    }

    @GetMapping
    public DocumentacionPersonaResponse documentacion(@PathVariable UUID personalId,
                                                      @RequestParam(defaultValue = "false") boolean historial) {
        return servicio.documentacion(personalId, historial);
    }

    @GetMapping("/{documentoId}/url")
    public Map<String, String> url(@PathVariable UUID personalId, @PathVariable UUID documentoId) {
        return Map.of("url", servicio.urlDescarga(personalId, documentoId));
    }
}
