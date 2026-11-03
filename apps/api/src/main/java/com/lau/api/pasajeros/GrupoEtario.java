package com.lau.api.pasajeros;

/**
 * Grupos de edad sin traslapes, basados en el curso de vida de MinSalud, para que cada pasajero
 * caiga en un único grupo y los datos sean comparables con estadísticas oficiales.
 */
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

    /** null significa "en adelante". */
    public Integer edadMaxima() {
        return edadMaxima;
    }
}
