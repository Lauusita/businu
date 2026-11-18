package com.lau.api.dominio.enums.operacion;

import lombok.Getter;

public enum EstadoOperacion {
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    SUSPENDIDA("Suspendida");


    @Getter
    private final String descripcion;
    EstadoOperacion(String descripcion) {
        this.descripcion = descripcion;
    }
}
