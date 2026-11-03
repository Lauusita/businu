package com.lau.api.flota;

import com.lau.api.compartido.BaseEntity;
import com.lau.api.compartido.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Motor fuera de borda (Suzuki 200 HP, dos por embarcación). Cada motor lleva su propio
 * horómetro y su propio intervalo de servicio periódico (100 h por defecto).
 */
@Getter
@Setter
@Entity
@Table(name = "motor", uniqueConstraints = @UniqueConstraint(columnNames = {"embarcacion_id", "posicion"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Motor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PosicionMotor posicion;

    @Column(nullable = false, length = 40)
    private String marca;

    @Column(length = 60)
    private String modelo;

    @Column(nullable = false, unique = true, length = 60)
    private String serial;

    @Column(name = "potencia_hp", nullable = false)
    private int potenciaHp;

    @Setter(AccessLevel.NONE)
    @Column(name = "horometro_actual", nullable = false, precision = 10, scale = 1)
    private BigDecimal horometroActual;

    @Column(name = "intervalo_servicio_horas", nullable = false)
    private int intervaloServicioHoras;

    Motor(Embarcacion embarcacion, PosicionMotor posicion, String marca, String modelo, String serial, int potenciaHp) {
        this.embarcacion = embarcacion;
        this.posicion = posicion;
        this.marca = marca;
        this.modelo = modelo;
        this.serial = serial;
        this.potenciaHp = potenciaHp;
        this.horometroActual = BigDecimal.ZERO;
        this.intervaloServicioHoras = 100;
    }

    /**
     * Registra una lectura del horómetro. Hoy la hace el mantenimiento o un ajuste manual;
     * cuando exista la bitácora (4.1), cada viaje la actualizará.
     */
    public void registrarLectura(BigDecimal lectura) {
        if (lectura.compareTo(horometroActual) < 0) {
            throw new ReglaNegocioException("La lectura del horómetro (" + lectura
                    + ") no puede ser menor que la actual (" + horometroActual + ") del motor " + serial);
        }
        this.horometroActual = lectura;
    }

    /**
     * Para mantenimientos: se permite registrar intervenciones pasadas (lectura menor), pero si
     * la lectura es mayor que la actual, el horómetro del motor avanza.
     */
    public void actualizarSiEsMayor(BigDecimal lectura) {
        if (lectura.compareTo(horometroActual) > 0) {
            this.horometroActual = lectura;
        }
    }

    public BigDecimal proximoServicioDesde(BigDecimal horometroServicio) {
        return horometroServicio.add(BigDecimal.valueOf(intervaloServicioHoras));
    }
}
