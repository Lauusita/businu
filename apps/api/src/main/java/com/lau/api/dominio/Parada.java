package com.lau.api.dominio;

import com.lau.api.dominio.enums.Embarcaderos;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Hora de ingreso y salida en cada embarcadero del recorrido (§4.1). Tabla operacion_paradas. */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Parada {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Embarcaderos embarcadero;

    @Column(name = "hora_llegada")
    private Instant horaLlegada;       // null en el embarcadero de origen

    @Column(name = "hora_salida")
    private Instant horaSalida;        // null en el embarcadero de destino
}