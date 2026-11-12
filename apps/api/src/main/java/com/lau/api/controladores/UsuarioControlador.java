package com.lau.api.controladores;

import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.usuario.UsuarioRequest;
import com.lau.api.dto.usuario.UsuarioResponse;
import com.lau.api.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("pasajeros")
public class UsuarioControlador {
    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }
    @GetMapping
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioServicio.obtenerTodos();
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
