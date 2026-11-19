package com.lau.api.dominio.enums.personal;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

public enum TipoIdentificacion {
    CC,
    CE,
    PPT,
    PASAPORTE;

    public static TipoIdentificacion fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Tipo de identificación inválido: " + value);
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de identificación inválido: " + value);
        }
    }
}
