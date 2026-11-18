package com.lau.api.dominio;

import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dominio.enums.personal.TipoIdentificacion;
import com.lau.api.dominio.enums.personal.TipoVinculacion;
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

    @Column(name = "numero_identificacion", nullable = false, length = 20, unique = true)
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

    @Column(nullable = false)
    private boolean disponibilidad;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    @Column(name = "contrato_activo")
    private boolean contratoActivo;

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
        this.disponibilidad = true;
        this.passwordHash = passwordHash;
        this.contratoActivo = true;
    }

    public void habilitarAcceso(String passwordHash) {
        if (!rol.usaSistema()) {
            throw new BusinessException("Los guías turísticos no tienen acceso al sistema");
        }
        this.passwordHash = passwordHash;
    }

    public void devolverDisponibilidad() {
        this.disponibilidad = true;
    }
    public void revocarAcceso() {
        this.passwordHash = null;
    }

    public void desactivarPersonal() {
        this.contratoActivo = false;
        this.setFechaRetiro(LocalDate.now());
    }

    public boolean tieneAcceso() {
        return disponibilidad && passwordHash != null;
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }


}