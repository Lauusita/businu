package com.lau.api.controladores;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.Usuario;
import com.lau.api.dto.PersonalAuth;
import com.lau.api.dto.UsuarioRequest;
import com.lau.api.dto.UsuarioResponse;
import com.lau.api.mappers.UsuarioMapper;
import com.lau.api.servicios.EmbarcacionServicio;
import com.lau.api.servicios.PersonalServicio;
import com.lau.api.servicios.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@RequestMapping("pasajeros")
public class UsuarioControlador {
    private final EmbarcacionServicio embarcacionServicio;
    private final UsuarioServicio usuarioServicio;
    private final PersonalServicio personalServicio;
    private final UsuarioMapper usuarioMapper;

    public UsuarioControlador(EmbarcacionServicio embarcacionServicio, PersonalServicio personalServicio, UsuarioServicio usuarioServicio, UsuarioMapper usuarioMapper) {
        this.embarcacionServicio = embarcacionServicio;
        this.personalServicio = personalServicio;
        this.usuarioServicio = usuarioServicio;
        this.usuarioMapper = usuarioMapper;
    }
    @GetMapping("")
    public List<Usuario> obtenerTodos() {
        return usuarioServicio.findAll();
    }

    @PostMapping("/crear")
    public UsuarioResponse registrarPasajeros(
            @Valid @RequestBody UsuarioRequest usuario,
            @AuthenticationPrincipal PersonalAuth auth
    ) {
        UUID idEmbarcacion = usuario.embarcacionId();
        Embarcacion embarcacion = embarcacionServicio.buscar(idEmbarcacion);
        Personal personal = personalServicio.buscarPorId(auth.id());
        Usuario usuarioMapped = usuarioMapper.requestToUsuario(usuario, embarcacion, personal);
        usuarioServicio.guardar(usuarioMapped);
        return usuarioMapper.toResponse(usuarioMapped);
    }
}
