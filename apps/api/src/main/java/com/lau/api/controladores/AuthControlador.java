package com.lau.api.controladores;

import com.lau.api.dto.login.LoginRequest;
import com.lau.api.dto.login.LoginResponse;
import com.lau.api.dto.login.RestablecerContrasenaRequest;
import com.lau.api.dto.login.SolicitudRecuperacionRequest;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.dto.personal.PersonalResponse;
import com.lau.api.servicios.AuthServicio;
import com.lau.api.servicios.RecuperacionContrasenaServicio;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthControlador {

    private final AuthServicio servicio;
    private final RecuperacionContrasenaServicio recuperacion;

    public AuthControlador(AuthServicio servicio, RecuperacionContrasenaServicio recuperacion) {
        this.servicio = servicio;
        this.recuperacion = recuperacion;
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

    /** Siempre responde lo mismo, exista o no la cuenta. */
    @PostMapping("/request-password-change")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> requestPasswordChange(@Valid @RequestBody SolicitudRecuperacionRequest request) {
        recuperacion.solicitar(request.numeroIdentificacion());
        return Map.of("mensaje",
                "Si los datos corresponden a una cuenta con correo registrado, enviamos un enlace para restablecer la contraseña.");
    }

    /** Solo funciona con el token del enlace enviado al correo. */
    @PostMapping("/new-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody RestablecerContrasenaRequest request) {
        recuperacion.restablecer(request.token(), request.nuevaContrasena());
    }
}
