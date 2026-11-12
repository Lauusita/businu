package com.lau.api.controladores;

import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.EstadoOperacion;
import com.lau.api.dto.operacion.*;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.OperacionServicio;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Bitácora de viaje (§4.1): zarpes, paradas y cambios de estado. */
@RestController
@RequestMapping("/operaciones")
public class OperacionControlador {

    private final OperacionServicio servicio;

    public OperacionControlador(OperacionServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<OperacionResponse> crear(@Valid @RequestBody CrearOperacionRequest request,
                                                   @AuthenticationPrincipal PersonalAuth auth) {
        OperacionResponse creada = servicio.crear(request, auth.id());
        return ResponseEntity.created(URI.create("/operaciones/" + creada.id())).body(creada);
    }

    @GetMapping("/{id}")
    public OperacionResponse obtener(@PathVariable UUID id) {
        return servicio.obtener(id);
    }

    @GetMapping
    public PagedModel<OperacionResumen> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoOperacion estado,
            @RequestParam(required = false) Embarcaciones embarcacion,
            @PageableDefault(size = 20, sort = "fechaHoraInicio", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(servicio.listar(fecha, estado, embarcacion, pageable));
    }

    @GetMapping("/abiertas")
    public List<OperacionResumen> abiertas(@RequestParam(required = false) Embarcaderos embarcadero) {
        return servicio.abiertasEn(embarcadero);
    }

    @PatchMapping("/{id}")
    public OperacionResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarOperacionRequest request,
                                        @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.actualizar(id, request, auth.id());
    }

    @PostMapping("/{id}/tripulacion/{personalId}")
    public OperacionResponse agregarTripulante(@PathVariable UUID id, @PathVariable UUID personalId,
                                               @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.agregarTripulante(id, personalId, auth.id());
    }

    @PostMapping("/{id}/paradas")
    public OperacionResponse registrarParada(@PathVariable UUID id, @Valid @RequestBody ParadaRequest request,
                                             @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.registrarParada(id, request, auth.id());
    }

    @PostMapping("/{id}/finalizar")
    public OperacionResponse finalizar(@PathVariable UUID id, @Valid @RequestBody FinalizarOperacionRequest request,
                                       @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.finalizar(id, request, auth.id());
    }

    @PostMapping("/{id}/suspender")
    public OperacionResponse suspender(@PathVariable UUID id, @Valid @RequestBody SuspenderOperacionRequest request,
                                       @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.suspender(id, request, auth.id());
    }
}