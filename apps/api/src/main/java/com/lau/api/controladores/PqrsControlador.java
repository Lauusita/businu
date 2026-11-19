package com.lau.api.controladores;

import com.lau.api.dominio.enums.pqrs.EstadoPqrs;
import com.lau.api.dominio.enums.pqrs.TipoPqrs;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.pqrs.*;
import com.lau.api.servicios.PqrsServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * PQRS (§4.7). POST /pqrs/radicar es público (formulario del código QR, en /pqrs). La consulta y la
 * gestión son solo del supervisor; el resto del personal no tiene acceso.
 */
@RestController
@RequestMapping("/pqrs")
public class PqrsControlador {

    private final PqrsServicio servicio;

    public PqrsControlador(PqrsServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/radicar")
    public ResponseEntity<RadicadoResponse> radicar(@Valid @RequestBody RadicarPqrsRequest request,
                                                    HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.radicar(request, ipCliente(http)));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @GetMapping("/gestion")
    public PagedModel<PqrsResponse> listar(
            @RequestParam(required = false) EstadoPqrs estado,
            @RequestParam(required = false) TipoPqrs tipo,
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(servicio.listar(estado, tipo, pageable));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @GetMapping("/gestion/{id}")
    public PqrsResponse obtener(@PathVariable UUID id) {
        return servicio.obtener(id);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @PostMapping("/gestion/{id}")
    public PqrsResponse gestionar(@PathVariable UUID id, @Valid @RequestBody GestionarPqrsRequest request,
                                  @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.gestionar(id, request, auth.id());
    }

    /**
     * IP del cliente detrás del balanceador de App Runner: el último valor de X-Forwarded-For, que lo agrega
     * el balanceador. Los primeros los puede inventar el cliente para saltarse el límite de envíos.
     */
    private static String ipCliente(HttpServletRequest r) {
        String xff = r.getHeader("X-Forwarded-For");
        if (xff == null || xff.isBlank()) {
            return r.getRemoteAddr();
        }
        String[] ips = xff.split(",");
        return ips[ips.length - 1].trim();
    }
}
