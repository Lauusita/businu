package com.lau.api.controladores;

import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.EstadoSeguimiento;
import com.lau.api.dominio.enums.seguimiento.TipoRegistroInfraestructura;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.infraestructura.RegistroInfraestructuraResponse.*;
import com.lau.api.dto.infraestructura.*;
import com.lau.api.servicios.InfraestructuraServicio;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Registro y seguimiento de embarcaderos y ayudas a la navegación (§4.4). */
@RestController
@RequestMapping("/infraestructura")
public class InfraestructuraControlador {

    private final InfraestructuraServicio servicio;

    public InfraestructuraControlador(InfraestructuraServicio servicio) {
        this.servicio = servicio;
    }

    // ---------------------------------------------------------------- Ayudas a la navegación

    @PostMapping("/ayudas")
    public ResponseEntity<AyudaResumen> crearAyuda(@Valid @RequestBody CrearAyudaNavegacionRequest request,
                                                                                   @AuthenticationPrincipal PersonalAuth auth) {
        AyudaResumen creada = servicio.crearAyuda(request, auth.id());
        return ResponseEntity.created(URI.create("/infraestructura/ayudas/" + creada.id())).body(creada);
    }

    @GetMapping("/ayudas")
    public List<AyudaResumen> listarAyudas() {
        return servicio.listarAyudasActivas().stream()
                .map(a -> new AyudaResumen(a.getId(), a.getCodigo(), a.getEstructura(), a.getFuncion(), a.isLuminosa()))
                .toList();
    }

    @PostMapping("/ayudas/{id}/desactivar")
    public ResponseEntity<Void> desactivarAyuda(@PathVariable UUID id, @AuthenticationPrincipal PersonalAuth auth) {
        servicio.desactivarAyuda(id, auth.id());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- Registros

    @PostMapping("/registros")
    public ResponseEntity<RegistroInfraestructuraResponse> registrar(
            @Valid @RequestBody CrearRegistroInfraestructuraRequest request,
            @AuthenticationPrincipal PersonalAuth auth) {
        RegistroInfraestructuraResponse creado = servicio.registrar(request, auth.id());
        return ResponseEntity.created(URI.create("/infraestructura/registros/" + creado.id())).body(creado);
    }

    @GetMapping("/registros/{id}")
    public RegistroInfraestructuraResponse obtener(@PathVariable UUID id) {
        return servicio.obtener(id);
    }

    @GetMapping("/registros")
    public PagedModel<RegistroInfraestructuraResponse> listar(
            @RequestParam(required = false) EstadoSeguimiento estado,
            @RequestParam(required = false) Embarcaderos embarcadero,
            @RequestParam(required = false) UUID ayudaId,
            @RequestParam(required = false) TipoRegistroInfraestructura tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(servicio.listar(estado, embarcadero, ayudaId, tipo, desde, hasta, pageable));
    }

    /** Lo que el supervisor debe informar al supervisor del contrato. */
    @GetMapping("/registros/pendientes-comunicar")
    public List<RegistroInfraestructuraResponse> pendientesDeComunicar() {
        return servicio.pendientesDeComunicar();
    }

    @PostMapping("/registros/{id}/comunicar")
    public RegistroInfraestructuraResponse comunicar(@PathVariable UUID id,
                                                     @Valid @RequestBody ComunicarSupervisorRequest request,
                                                     @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.comunicarSupervisor(id, request, auth.id());
    }

    @PostMapping("/registros/{id}/cerrar")
    public RegistroInfraestructuraResponse cerrar(@PathVariable UUID id,
                                                  @Valid @RequestBody CerrarRegistroRequest request,
                                                  @AuthenticationPrincipal PersonalAuth auth) {
        return servicio.cerrar(id, request, auth.id());
    }

    // ---------------------------------------------------------------- Tablero

    /** Estado actual de cada embarcadero y ayuda activa, con sus registros abiertos. */
    @GetMapping("/tablero")
    public List<EstadoInfraestructuraResponse> tablero() {
        return servicio.tablero();
    }
}