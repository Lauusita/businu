package com.lau.api.dominio.enums;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

public enum Rol {
    SUPERVISOR,
    GUIA_TURISTICA,
    OPERADOR_EMBARCACION,
    TAQUILLERO;

    public boolean usaSistema() {
        return this != GUIA_TURISTICA;
    }

    public static Rol fromString(String value) {
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
