package com.lau.api.dominio.enums;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

/** Modalidad de vinculación del personal (§6 del anexo). */
public enum TipoVinculacion {
    LABORAL,
    PRESTACION_SERVICIOS;

    public static TipoVinculacion fromString(String value) {
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
