package com.lau.api.dominio.enums;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

/** Estado operativo actual de la embarcación. */
public enum EstadoEmbarcacion {
    OPERATIVA,
    EN_MANTENIMIENTO,
    FUERA_DE_SERVICIO;

    public static EstadoEmbarcacion fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Estado inválido: " + value);
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inválido: " + value);
        }
    }
}
