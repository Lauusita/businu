package com.lau.api.dominio.enums.operacion;

import com.lau.api.exceptions.BusinessException;

import java.util.Locale;

/** Estado operativo actual de la embarcación. */
public enum EstadoEmbarcacion {
    OPERATIVA("Operativa"),
    EN_MANTENIMIENTO("En mantenimiento"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    private String descripcion;
    EstadoEmbarcacion(String descripcion) {
        this.descripcion = descripcion;
    }
}
