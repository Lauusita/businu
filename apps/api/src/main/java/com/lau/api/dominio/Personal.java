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
@Entity
@Table(name = "personal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_identificacion", "numero_identificacion"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Personal extends BaseEntity {

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_identificacion", nullable = false, length = 10)
    private TipoIdentificacion tipoIdentificacion;

    @Setter
    @Column(name = "numero_identificacion", nullable = false, length = 20, unique = true)
    private String numeroIdentificacion;

    @Setter
    @Column(nullable = false, length = 80)
    private String nombres;

    @Setter
    @Column(nullable = false, length = 80)
    private String apellidos;

    @Setter
    @Column(length = 120, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculacion", nullable = false, length = 30)
    private TipoVinculacion tipoVinculacion;

    @Setter
    @Column(name = "fecha_vinculacion", nullable = false)
    private LocalDate fechaVinculacion;

    @Setter
    @Column(name = "fecha_retiro")
    private LocalDate fechaRetiro;

    @Setter
    @Column(nullable = false)
    private boolean disponibilidad;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Setter
    @Column(name = "ultimo_acceso")
    private Instant ultimoAcceso;

    @Setter
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
        this.revocarAcceso();
    }

    public boolean tieneAcceso() {
        return contratoActivo && passwordHash != null;
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }


}