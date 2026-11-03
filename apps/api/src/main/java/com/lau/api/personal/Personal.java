package com.lau.api.personal;

import com.lau.api.compartido.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Personal vinculado a la operación (§4.6). Incluye a los guías, aunque no usen el sistema. */
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

    @Column(length = 20)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Cargo cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vinculacion", nullable = false, length = 30)
    private TipoVinculacion tipoVinculacion;

    @Column(name = "fecha_vinculacion", nullable = false)
    private LocalDate fechaVinculacion;

    @Column(name = "fecha_retiro")
    private LocalDate fechaRetiro;

    /** §7.3.2: si la persona reemplaza a otra del equipo mínimo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reemplaza_a_id")
    private Personal reemplazaA;

    @Column(nullable = false)
    private boolean activo;

    public Personal(TipoIdentificacion tipoIdentificacion, String numeroIdentificacion, String nombres,
                    String apellidos, Cargo cargo, TipoVinculacion tipoVinculacion, LocalDate fechaVinculacion) {
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cargo = cargo;
        this.tipoVinculacion = tipoVinculacion;
        this.fechaVinculacion = fechaVinculacion;
        this.activo = true;
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
