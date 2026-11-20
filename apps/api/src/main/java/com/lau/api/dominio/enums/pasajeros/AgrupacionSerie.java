package com.lau.api.dominio.enums.pasajeros;

/** Unidad de la serie de tiempo del consolidado (R22). */
public enum AgrupacionSerie {
    DIA("day"),
    SEMANA("week"),     // la semana empieza el lunes
    MES("month");

    private final String sql;

    AgrupacionSerie(String sql) {
        this.sql = sql;
    }

    /** Unidad para date_trunc de PostgreSQL. */
    public String sql() {
        return sql;
    }
}
