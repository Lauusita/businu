package com.lau.api.dominio.enums;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

public enum TipoIdentificacion {
    CC,
    CE,
    PPT,
    PASAPORTE;

    public static TipoIdentificacion fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Rol inválido: " + value);
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rol inválido: " + value);
        }
    }
}
