package com.lau.api.config;

import com.lau.api.dominio.enums.Rol;
import com.lau.api.dto.personal.PersonalAuth;
import com.lau.api.exceptions.TokenExpiredException;
import com.lau.api.exceptions.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final Duration duracion;

    public TokenUtils(@Value("${security.secret-key}") String secretKey,
                      @Value("${security.access-token-minutes:480}") long minutos) {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("security.secret-key debe tener al menos 32 caracteres (HS256)");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String crearToken(UUID personalId, Rol rol) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(personalId.toString())
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }

    public long segundosDeVigencia() {
        return duracion.toSeconds();
    }

    /** Valida firma y vencimiento, y arma la autenticación que llega a @AuthenticationPrincipal. */
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("El token ha expirado");
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Token inválido");
        }

        String subject = claims.getSubject();
        String rolTexto = claims.get(CLAIM_ROL, String.class);
        if (subject == null || rolTexto == null) {
            throw new UnauthorizedException("Token incompleto");
        }

        Rol rol = Rol.valueOf(rolTexto);
        PersonalAuth principal = new PersonalAuth(rol, UUID.fromString(subject));
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
    }
}