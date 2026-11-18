package com.lau.api.dominio.enums.operacion;


import lombok.Getter;

public enum Embarcaderos {
    RANCHO_GRANDE("Rancho Grande"),
    CENTRO_CALLE_22("Centro Calle 22"),
    CENTRO_VERDE("Centro Verde");
    // UNIVERSIDAD_DE_CORDOBA; no entra en prueba piloto

    @Getter
    private final String descripcion;

    Embarcaderos(String descripcion) {
        this.descripcion = descripcion;
    }
}
