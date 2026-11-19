package com.lau.api.dominio.enums.pasajeros;

import lombok.Getter;

/**
 * Franjas del día para analizar la demanda (R22), dentro del horario de operación (05:00 a 21:00, §7.1.1).
 * Un abordaje antes de las 05:00 cuenta en la mañana y uno después de las 21:00 en la noche.
 */
@Getter
public enum FranjaHoraria {
    MANANA("05:00", "11:59"),
    TARDE("12:00", "17:59"),
    NOCHE("18:00", "21:00");

    private final String desde;
    private final String hasta;

    FranjaHoraria(String desde, String hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public static FranjaHoraria de(int hora) {
        if (hora < 12) {
            return MANANA;
        }
        return hora < 18 ? TARDE : NOCHE;
    }
}
