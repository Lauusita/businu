package com.lau.api.dominio;

import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Horómetro de un motor en un viaje (§4.5, R37): lectura al zarpar y al terminar.
 * Las horas de motor del viaje son la diferencia. Tabla operacion_horometros.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LecturaHorometro {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motor_id", nullable = false)
    private Motor motor;

    @Column(name = "lectura_inicial", nullable = false, precision = 10, scale = 1)
    private BigDecimal lecturaInicial;

    /** Null mientras el viaje no termina o si no se registró la lectura final. */
    @Column(name = "lectura_final", precision = 10, scale = 1)
    private BigDecimal lecturaFinal;

    LecturaHorometro(Motor motor, BigDecimal lecturaInicial) {
        this.motor = motor;
        this.lecturaInicial = lecturaInicial;
    }

    void registrarFinal(BigDecimal lectura) {
        if (lectura.compareTo(lecturaInicial) < 0) {
            throw new BusinessException("La lectura final del horómetro del motor " + motor.getSerial() + " ("
                    + lectura + ") no puede ser menor que la inicial (" + lecturaInicial + ")");
        }
        this.lecturaFinal = lectura;
    }

    /** Horas de motor del viaje; null si falta la lectura final. */
    public BigDecimal horas() {
        return lecturaFinal == null ? null : lecturaFinal.subtract(lecturaInicial);
    }
}
