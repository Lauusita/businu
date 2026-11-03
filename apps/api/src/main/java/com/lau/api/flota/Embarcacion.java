package com.lau.api.flota;

import com.lau.api.compartido.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embarcación del sistema (Betancí, Yapel). La patente es la identificación oficial y es única,
 * pero no es la llave primaria: puede corregirse sin romper las relaciones.
 */
@Getter
@Setter
@Entity
@Table(name = "embarcacion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Embarcacion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String patente;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "capacidad_pasajeros", nullable = false)
    private int capacidadPasajeros;

    @Column(length = 100)
    private String constructor;

    @Column(name = "anio_construccion")
    private Integer anioConstruccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEmbarcacion estado;

    /** Horas de operación diarias proyectadas: 6 recorridos de 1 hora, lunes a viernes. */
    @Column(name = "horas_operacion_dia", nullable = false, precision = 4, scale = 1)
    private BigDecimal horasOperacionDia;

    @OneToMany(mappedBy = "embarcacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion")
    @Setter(AccessLevel.NONE)
    private List<Motor> motores = new ArrayList<>();

    public Embarcacion(String patente, String nombre, int capacidadPasajeros) {
        this.patente = patente;
        this.nombre = nombre;
        this.capacidadPasajeros = capacidadPasajeros;
        this.estado = EstadoEmbarcacion.OPERATIVA;
        this.horasOperacionDia = new BigDecimal("6.0");
    }

    public List<Motor> getMotores() {
        return Collections.unmodifiableList(motores);
    }

    public Motor agregarMotor(PosicionMotor posicion, String marca, String modelo, String serial, int potenciaHp) {
        boolean ocupada = motores.stream().anyMatch(m -> m.getPosicion() == posicion);
        if (ocupada) {
            throw new IllegalArgumentException("La embarcación ya tiene un motor en " + posicion);
        }
        Motor motor = new Motor(this, posicion, marca, modelo, serial, potenciaHp);
        motores.add(motor);
        return motor;
    }
}
