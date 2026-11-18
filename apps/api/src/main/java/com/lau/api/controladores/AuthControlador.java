package com.lau.api.controladores;

import com.lau.api.dto.login.LoginResponse;
import com.lau.api.dto.login.LoginRequest;
import com.lau.api.dto.personal.PersonalAuth;
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

    @GetMapping
    public void getDetails(
            @AuthenticationPrincipal PersonalAuth personalAuth) {

    }
}