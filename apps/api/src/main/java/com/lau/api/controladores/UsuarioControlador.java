package com.lau.api.controladores;

import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pasajeros.AgrupacionSerie;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.usuario.ConsolidadoDemandaResponse;
import com.lau.api.servicios.ConsolidadoDemandaServicio;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import com.lau.api.dto.usuario.UsuarioFilter;
import com.lau.api.dto.usuario.UsuarioRequest;
import com.lau.api.dto.usuario.UsuarioResponse;
import com.lau.api.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pasajeros")
public class UsuarioControlador {
    private final UsuarioServicio usuarioServicio;
    private final ConsolidadoDemandaServicio consolidadoServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio, ConsolidadoDemandaServicio consolidadoServicio) {
        this.usuarioServicio = usuarioServicio;
        this.consolidadoServicio = consolidadoServicio;
    }

    // UsuarioControlador
    @GetMapping
    public PagedModel<UsuarioResponse> buscar(
            @ModelAttribute UsuarioFilter filtro,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(usuarioServicio.buscar(filtro, pageable));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrarPasajeros(
            @Valid @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal @NonNull PersonalAuth auth
    ) {
        UsuarioResponse creado = usuarioServicio.registrar(request, auth.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Consolidado de la demanda (§4.2, R22): totales, sexo, grupo etario, origen, destino, franja horaria,
     * hora y serie de tiempo. Por defecto, el mes en curso agrupado por día.
     */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @GetMapping("/consolidado")
    public ConsolidadoDemandaResponse consolidado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Embarcaciones embarcacion,
            @RequestParam(required = false) Embarcaderos origen,
            @RequestParam(required = false) AgrupacionSerie agrupacion) {
        return consolidadoServicio.consolidar(desde, hasta, embarcacion, origen, agrupacion);
    }

    @GetMapping("/operacion/{id}")
    public List<UsuarioResponse> obtenerPorOp(
            @PathVariable UUID id
    ) {
        return usuarioServicio.obtenerPorOperacion(id);
    }
}
