package com.lau.api.dominio;

import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.EstadoEmbarcacion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "embarcacion")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Embarcacion extends BaseEntity {
    @Column(name = "numero_identificador_construccion")
    private String nic;

    @Column(nullable = false, unique = true, length = 40)
    private String patente;

    // BETANCÍ Y YAPEL
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 60)
    private Embarcaciones nombre;

    @Column(name = "capacidad_pasajeros", nullable = false)
    private int capacidadPasajeros;

    @Column(length = 100)
    private String constructor;

    @Column(name = "anio_construccion")
    private int anioConstruccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEmbarcacion estado;

    /** Horas de operación diarias proyectadas: 6 recorridos de 1 hora, lunes a viernes. */
    @Column(name = "horas_operacion_dia", nullable = false, precision = 4, scale = 1)
    private BigDecimal horasOperacionDia;

    @OneToMany(mappedBy = "embarcacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Motor> motores = new ArrayList<>();

    public Motor agregarMotor(String marca, String modelo, String serial, int potenciaHp) {
        Motor motor = new Motor(this, marca, modelo, serial, potenciaHp);
        motores.add(motor);
        return motor;
    }

    public Embarcacion(String patente, Embarcaciones nombre, int capacidadPasajeros) {
        this.patente = patente;
        this.nombre = nombre;
        this.capacidadPasajeros = capacidadPasajeros;
        this.estado = EstadoEmbarcacion.OPERATIVA;
        this.horasOperacionDia = new BigDecimal("6.0");
    }
}