package com.lau.api.servicios;

import com.lau.api.config.TokenUtils;
import com.lau.api.dominio.Personal;
import com.lau.api.dto.LoginRequest;
import com.lau.api.dto.LoginResponse;
import com.lau.api.exceptions.UnauthorizedException;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServicio {

    private final PersonalRepositorio personal;
    private final PasswordEncoder encoder;
    private final TokenUtils tokenUtils;

    public AuthServicio(PersonalRepositorio personal, PasswordEncoder encoder, TokenUtils tokenUtils) {
        this.personal = personal;
        this.encoder = encoder;
        this.tokenUtils = tokenUtils;
    }

    @Transactional
    public LoginResponse login(LoginRequest r) {
        Personal p = personal.findByNumeroIdentificacion(r.numeroIdentificacion().trim())
                .filter(Personal::tieneAcceso)                                   // activo y con contraseña
                .filter(x -> encoder.matches(r.contrasena(), x.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        p.setUltimoAcceso(Instant.now());
        String token = tokenUtils.crearToken(p.getId(), p.getRol());
        return new LoginResponse(token, "Bearer", tokenUtils.segundosDeVigencia(),
                p.getId(), p.nombreCompleto(), p.getRol());
    }
}