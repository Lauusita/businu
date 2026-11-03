package com.lau.api.domain;

import com.lau.api.domain.enums.Cargo;
import com.lau.api.domain.enums.Rol;
import com.lau.api.exceptions.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuenta de acceso al sistema (0..1 por persona). El cargo dice qué hace la persona en la
 * operación; el rol dice qué puede hacer en el sistema.
 */
@Getter
@Setter
@Entity
@Table(name = "usuario_sistema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioSistema extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false, unique = true)
    private Personal personal;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    public UsuarioSistema(Personal personal, String username, String passwordHash, Rol rol) {
        if (personal.getCargo() == Cargo.GUIA_TURISMO) {
            throw new ReglaNegocioException("Los guías de turismo no tienen acceso al sistema");
        }
        this.personal = personal;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
    }
}
