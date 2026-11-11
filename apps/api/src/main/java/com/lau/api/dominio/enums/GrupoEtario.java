package com.lau.api.dominio.enums;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

public enum GrupoEtario {
    PRIMERA_INFANCIA(0, 5),
    INFANCIA(6, 11),
    ADOLESCENCIA(12, 17),
    JUVENTUD(18, 28),
    ADULTEZ(29, 59),
    PERSONA_MAYOR(60, null);

    private final int edadMinima;
    private final Integer edadMaxima;

    GrupoEtario(int edadMinima, Integer edadMaxima) {
        this.edadMinima = edadMinima;
        this.edadMaxima = edadMaxima;
    }

    public int edadMinima() {
        return edadMinima;
    }

    /** Null significa "en adelante". */
    public Integer edadMaxima() {
        return edadMaxima;
    }

    public static GrupoEtario fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Grupo inválido: " + value);
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_");

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Grupo inválido: " + value);
        }
    }
}