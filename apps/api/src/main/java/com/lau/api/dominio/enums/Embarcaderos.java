package com.lau.api.dominio.enums;


import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

public enum Embarcaderos {
    RANCHO_GRANDE,
    CENTRO_CALLE_22,
    CENTRO_VERDE,
    UNIVERSIDAD_DE_CORDOBA;

    public static Embarcaderos fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Embarcadero inválido: " + value);
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Embarcadero inválido: " + value);
        }
    }
}
