package com.lau.api.dominio.enums.mantenimiento;

import lombok.Getter;

public enum SistemaIntervenido {
    MOTOR("Motor"),
    CASCO("Casco"),
    ELECTRICO("Eléctrico"),
    AIRE_ACONDICIONADO("Aire Acondicionado"),
    SEGURIDAD("Seguridad"),
    OTRO("Otro");

    @Getter
    private final String descripcion;
    SistemaIntervenido(String descripcion) {
        this.descripcion = descripcion;
    }
}

