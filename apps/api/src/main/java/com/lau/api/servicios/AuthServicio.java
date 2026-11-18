package com.lau.api.servicios;

import com.lau.api.config.TokenUtils;
import com.lau.api.dominio.Personal;
import com.lau.api.dto.login.LoginRequest;
import com.lau.api.dto.login.LoginResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.exceptions.UnauthorizedException;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.Instant;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServicio {

    private final PersonalRepositorio personal;
    private final PasswordEncoder encoder;
    private final TokenUtils tokenUtils;
    private final String hashFalso;

    public AuthServicio(PersonalRepositorio personal, PasswordEncoder encoder, TokenUtils tokenUtils) {
        this.personal = personal;
        this.encoder = encoder;
        this.tokenUtils = tokenUtils;
        this.hashFalso = encoder.encode("hash-que-realmente-nunca-existira");
    }

    @Transactional
    public LoginResponse login(LoginRequest r) {
        Optional<Personal> candidato = personal.findByNumeroIdentificacion(r.numeroIdentificacion().trim())
                .filter(x -> x.getRol().usaSistema())
                .filter(Personal::tieneAcceso);

        String hash = candidato.map(Personal::getPasswordHash).orElse(hashFalso);
        boolean ok = encoder.matches(r.contrasena(), hash);

        Personal p = candidato.filter(x -> ok)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        p.setUltimoAcceso(Instant.now());
        String token = tokenUtils.crearToken(p.getId(), p.getRol());
        return new LoginResponse(token, "Bearer", tokenUtils.segundosDeVigencia(),
                p.getId(), p.nombreCompleto(), p.getRol());
    }

//    public Personal me(LoginRequest r) {
//
//    }
}