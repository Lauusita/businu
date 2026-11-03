package com.lau.api.pasajeros;

import com.lau.api.compartido.EntidadHija;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Una celda de la cuadrícula: cuántas personas de un grupo etario y un sexo. Solo celdas mayores que cero. */
@Getter
@Entity
@Table(name = "registro_pasajeros_detalle",
        uniqueConstraints = @UniqueConstraint(columnNames = {"registro_id", "grupo_etario", "sexo"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistroPasajerosDetalle extends EntidadHija {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_id", nullable = false)
    private RegistroPasajeros registro;

    @Enumerated(EnumType.STRING)
    @Column(name = "grupo_etario", nullable = false, length = 20)
    private GrupoEtario grupoEtario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Sexo sexo;

    @Column(nullable = false)
    private int cantidad;

    RegistroPasajerosDetalle(RegistroPasajeros registro, GrupoEtario grupoEtario, Sexo sexo, int cantidad) {
        this.registro = registro;
        this.grupoEtario = grupoEtario;
        this.sexo = sexo;
        this.cantidad = cantidad;
    }
}
