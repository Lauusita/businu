package com.lau.api.controladores;

import com.lau.api.dominio.enums.TipoRegistroAuditado;
import com.lau.api.dto.auditoria.RevisionResponse;
import com.lau.api.servicios.AuditoriaServicio;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Historial de cambios con autor (R5). */
@RestController
@RequestMapping("/auditoria")
@PreAuthorize("hasRole('SUPERVISOR')")
public class AuditoriaControlador {

    private final AuditoriaServicio servicio;

    public AuditoriaControlador(AuditoriaServicio servicio) {
        this.servicio = servicio;
    }

    /** Ej.: GET /auditoria/OPERACION/{id} */
    @GetMapping("/{tipo}/{id}")
    public List<RevisionResponse> historial(@PathVariable TipoRegistroAuditado tipo, @PathVariable UUID id) {
        return servicio.historial(tipo, id);
    }
}
