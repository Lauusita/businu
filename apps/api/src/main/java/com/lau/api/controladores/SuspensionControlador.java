package com.lau.api.controladores;

import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.suspension.LevantarSuspensionRequest;
import com.lau.api.dto.suspension.SuspenderEmbarcacionRequest;
import com.lau.api.dto.suspension.SuspensionResponse;
import com.lau.api.servicios.SuspensionServicio;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Suspensiones de una embarcación y su historial (§7.1.7, R48). */
@RestController
@RequestMapping("/embarcaciones/{embarcacionId}/suspensiones")
public class SuspensionControlador {

    private final SuspensionServicio servicio;

    public SuspensionControlador(SuspensionServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<SuspensionResponse> historial(@PathVariable UUID embarcacionId) {
        return servicio.historial(embarcacionId);
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR','OPERADOR_EMBARCACION')")
    @PostMapping
    public ResponseEntity<SuspensionResponse> suspender(@PathVariable UUID embarcacionId,
                                                        @Valid @RequestBody SuspenderEmbarcacionRequest request,
                                                        @AuthenticationPrincipal PersonalAuth auth) {
        SuspensionResponse s = servicio.suspender(embarcacionId, request, auth.id());
        return ResponseEntity.created(URI.create("/embarcaciones/" + embarcacionId + "/suspensiones/" + s.id())).body(s);
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR','OPERADOR_EMBARCACION')")
    @PostMapping("/{suspensionId}/levantar")
    public SuspensionResponse levantar(@PathVariable UUID embarcacionId, @PathVariable UUID suspensionId,
                                       @Valid @RequestBody LevantarSuspensionRequest request,
                                       @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.levantar(embarcacionId, suspensionId, request, auth.id());
    }
}
