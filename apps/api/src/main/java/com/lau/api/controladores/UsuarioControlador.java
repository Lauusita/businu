package com.lau.api.controladores;

import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.usuario.UsuarioRequest;
import com.lau.api.dto.usuario.UsuarioResponse;
import com.lau.api.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pasajeros")
public class UsuarioControlador {
    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public PagedModel<UsuarioResponse> obtenerTodos(
            @AuthenticationPrincipal PersonalAuth auth,
            @RequestParam(required = false) UUID embarcacionId,
            @RequestParam(required = false) Embarcaderos embarcadero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(usuarioServicio.obtenerTodos(embarcacionId, embarcadero, desde, hasta, pageable));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrarPasajeros(
            @Valid @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal PersonalAuth auth
    ) {
        UsuarioResponse creado = usuarioServicio.registrar(request, auth.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
