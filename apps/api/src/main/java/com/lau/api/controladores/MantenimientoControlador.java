package com.lau.api.controladores;

import com.lau.api.dto.mantenimiento.CrearMantenimientoRequest;
import com.lau.api.dto.mantenimiento.MantenimientoResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.MantenimientoServicio;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Intervenciones de mantenimiento de las embarcaciones (§4.3). */
@RestController
@RequestMapping("/mantenimientos")
public class MantenimientoControlador {

    private final MantenimientoServicio servicio;

    public MantenimientoControlador(MantenimientoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','OPERADOR_EMBARCACION')")
    public ResponseEntity<MantenimientoResponse> registrar(@Valid @RequestBody CrearMantenimientoRequest request,
                                                           @AuthenticationPrincipal PersonalAuth auth) {
        MantenimientoResponse creado = servicio.registrar(request, auth.id());
        return ResponseEntity.created(URI.create("/mantenimientos/" + creado.id())).body(creado);
    }

    @GetMapping("/{id}")
    public MantenimientoResponse obtener(@PathVariable UUID id) {
        return servicio.obtener(id);
    }
}