package com.lau.api.dominio.enums;


import com.lau.api.exceptions.BusinessException;
import lombok.Getter;

import java.util.Locale;

public enum Embarcaderos {
    RANCHO_GRANDE("Rancho Grande"),
    CENTRO_CALLE_22("Centro Calle 22"),
    CENTRO_VERDE("Centro Verde");
    // UNIVERSIDAD_DE_CORDOBA; no entra en prueba piloto

    @Getter
    private final String descripcion;

    Embarcaderos(String descripcion) {
        this.descripcion = descripcion;
    }
}
