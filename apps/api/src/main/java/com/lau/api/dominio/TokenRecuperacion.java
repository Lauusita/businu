package com.lau.api.dominio;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Enlace de un solo uso para restablecer la contraseña. Solo se guarda el hash del token. */
@Getter
@Entity
@Table(name = "token_recuperacion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRecuperacion {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, updatable = false)
    private Personal personal;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "expira_en", nullable = false, updatable = false)
    private Instant expiraEn;

    @Column(name = "usado_en")
    private Instant usadoEn;

    public TokenRecuperacion(Personal personal, String tokenHash, Instant creadoEn, Instant expiraEn) {
        this.id = UUID.randomUUID();
        this.personal = personal;
        this.tokenHash = tokenHash;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
    }

    public boolean vigente(Instant ahora) {
        return usadoEn == null && ahora.isBefore(expiraEn);
    }

    public void marcarUsado(Instant ahora) {
        this.usadoEn = ahora;
    }
}
