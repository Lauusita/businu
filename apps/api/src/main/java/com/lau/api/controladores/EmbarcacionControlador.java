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

    public EmbarcacionControlador(EmbarcacionServicio embarcacionServicio) {
        this.embarcacionServicio = embarcacionServicio;
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
}
