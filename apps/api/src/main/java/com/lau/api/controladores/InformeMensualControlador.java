package com.lau.api.controladores;

import com.lau.api.dto.informe.InformeMensualResponse;
import com.lau.api.dto.informe.RedactarInformeRequest;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.InformeMensualServicio;
import jakarta.validation.Valid;
import java.time.YearMonth;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Informe mensual de operación (§4.7). Solo supervisor. El mes va como yyyy-MM (p. ej. 2026-11). */
@RestController
@RequestMapping("/informes/mensual/{mes}")
@PreAuthorize("hasRole('SUPERVISOR')")
public class InformeMensualControlador {

    private final InformeMensualServicio servicio;

    public InformeMensualControlador(InformeMensualServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public InformeMensualResponse obtener(@PathVariable YearMonth mes) {
        return servicio.obtener(mes);
    }

    /** Crea o actualiza el texto del supervisor (solo en BORRADOR). */
    @PutMapping
    public InformeMensualResponse redactar(@PathVariable YearMonth mes, @Valid @RequestBody RedactarInformeRequest request) {
        return servicio.redactar(mes, request);
    }

    @PostMapping("/cerrar")
    public InformeMensualResponse cerrar(@PathVariable YearMonth mes, @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.cerrar(mes, auth.id());
    }

    @PostMapping("/entregar")
    public InformeMensualResponse entregar(@PathVariable YearMonth mes, @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.entregar(mes, auth.id());
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable YearMonth mes) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("businu-informe-mensual-" + mes + ".pdf").build().toString())
                .body(servicio.pdf(mes));
    }
}
