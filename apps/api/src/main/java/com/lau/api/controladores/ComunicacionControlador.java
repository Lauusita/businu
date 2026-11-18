package com.lau.api.controladores;

import com.lau.api.dominio.enums.comunicacion.TipoReferenciaFalla;
import com.lau.api.dto.comunicacion.ComunicacionRequest;
import com.lau.api.dto.comunicacion.ComunicacionResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.servicios.ComunicacionServicio;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Constancias de comunicación de fallas por correo (§7.1.12, R50). */
@RestController
@RequestMapping("/comunicaciones")
@PreAuthorize("hasAnyRole('SUPERVISOR','OPERADOR_EMBARCACION')")
public class ComunicacionControlador {

    private final ComunicacionServicio servicio;

    public ComunicacionControlador(ComunicacionServicio servicio) {
        this.servicio = servicio;
    }

    /** Registra la constancia e intenta el envío. 201 siempre; el estado (ENVIADO/FALLIDO) va en el cuerpo. */
    @PostMapping
    public ResponseEntity<ComunicacionResponse> registrar(@Valid @RequestBody ComunicacionRequest request,
                                                          @AuthenticationPrincipal PersonalAuth auth) {
        ComunicacionResponse c = servicio.registrar(request, auth.id());
        return ResponseEntity.created(URI.create("/comunicaciones/" + c.id())).body(c);
    }

    @PostMapping("/{id}/reintentar")
    public ComunicacionResponse reintentar(@PathVariable UUID id) {
        return servicio.reintentar(id);
    }

    @GetMapping
    public List<ComunicacionResponse> listar(@RequestParam TipoReferenciaFalla referenciaTipo,
                                             @RequestParam UUID referenciaId) {
        return servicio.listar(referenciaTipo, referenciaId);
    }
}
