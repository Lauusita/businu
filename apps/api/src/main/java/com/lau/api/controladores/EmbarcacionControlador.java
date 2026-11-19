package com.lau.api.controladores;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dto.embarcacion.EmbarcacionResponse;
import com.lau.api.dto.embarcacion.HojaDeVidaResponse;
import com.lau.api.servicios.EmbarcacionServicio;
import com.lau.api.servicios.MantenimientoServicio;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/embarcaciones")
public class EmbarcacionControlador {
    private final EmbarcacionServicio embarcacionServicio;
    private final com.lau.api.utilidades.GeneradorPdf generadorPdf;

    public EmbarcacionControlador(EmbarcacionServicio embarcacionServicio,
                                  com.lau.api.utilidades.GeneradorPdf generadorPdf) {
        this.embarcacionServicio = embarcacionServicio;
        this.generadorPdf = generadorPdf;
    }

    /** Embarcaciones con sus motores (ids para los formularios de operación, combustible y mantenimiento). */
    @GetMapping
    public List<EmbarcacionResponse> listar(
            @RequestParam(required = false) UUID embarcacionId
    ) {
        return embarcacionId == null ? embarcacionServicio.listar() : Collections.singletonList(embarcacionServicio.find(embarcacionId));
    }

    /** Hoja de vida (§4.3). */
    @GetMapping("/{id}/hoja-de-vida")
    public HojaDeVidaResponse hojaDeVida(@PathVariable UUID id) {
        return embarcacionServicio.hojaDeVida(id);
    }

    /** Hoja de vida en PDF (§4.3, §4.8). Solo supervisor. */
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPERVISOR')")
    @GetMapping("/{id}/hoja-de-vida/pdf")
    public org.springframework.http.ResponseEntity<byte[]> hojaDeVidaPdf(@PathVariable UUID id) {
        HojaDeVidaResponse h = embarcacionServicio.hojaDeVida(id);
        byte[] pdf = generadorPdf.generar("hoja-de-vida", java.util.Map.of("h", h, "fmt", new com.lau.api.utilidades.Formato(),
                "generada", java.time.Instant.now()));
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"hoja-de-vida-" + h.embarcacion().nombre() + ".pdf\"")
                .body(pdf);
    }
}
