package co.gov.monteria.businu.mantenimiento;

import co.gov.monteria.businu.compartido.EntidadHija;
import co.gov.monteria.businu.flota.Motor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Lectura del horómetro de cada motor intervenido (un mismo sábado se suelen atender los dos). */
@Getter
@Entity
@Table(name = "mantenimiento_motor", uniqueConstraints = @UniqueConstraint(columnNames = {"mantenimiento_id", "motor_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MantenimientoMotor extends EntidadHija {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mantenimiento_id", nullable = false)
    private Mantenimiento mantenimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motor_id", nullable = false)
    private Motor motor;

    @Column(nullable = false, precision = 10, scale = 1)
    private BigDecimal horometro;

    @Column(name = "servicio_periodico", nullable = false)
    private boolean servicioPeriodico;

    /** Fijo al guardar: horómetro + intervalo del motor. Null si no fue servicio periódico. */
    @Column(name = "proximo_horometro", precision = 10, scale = 1)
    private BigDecimal proximoHorometro;

    MantenimientoMotor(Mantenimiento mantenimiento, Motor motor, BigDecimal horometro,
                       boolean servicioPeriodico, BigDecimal proximoHorometro) {
        this.mantenimiento = mantenimiento;
        this.motor = motor;
        this.horometro = horometro;
        this.servicioPeriodico = servicioPeriodico;
        this.proximoHorometro = proximoHorometro;
    }
}
