package com.lau.api.controladores;

import com.lau.api.dto.personal.PersonalAuth;
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

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
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

    @GetMapping("/operacion/{id}")
    public List<UsuarioResponse> obtenerPorOp(
            @PathVariable UUID id
    ) {
        return usuarioServicio.obtenerPorOperacion(id);
    }
}
