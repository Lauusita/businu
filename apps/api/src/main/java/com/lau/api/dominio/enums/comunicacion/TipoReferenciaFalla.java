package com.lau.api.dominio.enums.comunicacion;

/** Registro que origina la comunicación de la falla o novedad (§7.1.12). */
public enum TipoReferenciaFalla {
    OPERACION,        // novedad o falla registrada en la bitácora del viaje
    SUSPENSION,       // suspensión de la embarcación
    MANTENIMIENTO     // necesidad de mantenimiento que afecta la operación (§7.1.8)
}
