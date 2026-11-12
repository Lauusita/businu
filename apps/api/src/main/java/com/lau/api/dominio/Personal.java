package com.lau.api.dominio;

import com.lau.api.dominio.enums.Rol;
import com.lau.api.dominio.enums.TipoIdentificacion;
import com.lau.api.dominio.enums.TipoVinculacion;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Personal de la operación (§4.6). Quien usa el sistema tiene username y contraseña; los guías no. */
@Getter
@Setter
@Entity
@Table(name = "personal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_identificacion", "numero_identificacion"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Personal extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_identificacion", nullable = false, length = 10)
    private TipoIdentificacion tipoIdentificacion;

    @Column(name = "numero_identificacion", nullable = false, length = 20)
    private String numeroIdentificacion;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(length = 120, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculacion", nullable = false, length = 30)
    private TipoVinculacion tipoVinculacion;

    @Column(name = "fecha_vinculacion", nullable = false)
    private LocalDate fechaVinculacion;

    @Column(name = "fecha_retiro")
    private LocalDate fechaRetiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reemplaza_a_id")
    private Personal reemplazaA;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    public Personal(TipoIdentificacion tipoIdentificacion, String numeroIdentificacion, String nombres,
                    String apellidos, Rol rol, TipoVinculacion tipoVinculacion, String email, LocalDate fechaVinculacion, String passwordHash) {
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rol = rol;
        this.email = email;
        this.tipoVinculacion = tipoVinculacion;
        this.fechaVinculacion = fechaVinculacion;
        this.activo = true;
        this.passwordHash = passwordHash;
    }

    public void habilitarAcceso(String username, String passwordHash) {
        if (!rol.usaSistema()) {
            throw new BusinessException("Los guías turísticos no tienen acceso al sistema");
        }
        this.numeroIdentificacion = username;
        this.passwordHash = passwordHash;
    }

    public void revocarAcceso() {
        this.numeroIdentificacion = null;
        this.passwordHash = null;
    }

    public boolean tieneAcceso() {
        return activo && numeroIdentificacion != null;
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }


}