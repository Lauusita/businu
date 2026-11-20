package com.lau.api.controladores;

import com.lau.api.dto.login.LoginRequest;
import com.lau.api.dto.login.LoginResponse;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.personal.PersonalResponse;
import com.lau.api.servicios.AuthServicio;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthControlador {

    private final AuthServicio servicio;

    public AuthControlador(AuthServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return servicio.login(request);
    }

    /** Usuario autenticado: la identidad sale del token, nunca de un parámetro del cliente. */
    @GetMapping("/me")
    public PersonalResponse me(@AuthenticationPrincipal PersonalAuth auth) {
        return servicio.me(auth.id());
    }
}
