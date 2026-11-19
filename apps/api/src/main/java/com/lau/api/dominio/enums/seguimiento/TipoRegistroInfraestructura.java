package com.lau.api.dominio.enums.seguimiento;

/** Qué tipo de registro es (§4.4). */
public enum TipoRegistroInfraestructura {
    REVISION,
    MANTENIMIENTO,
    NOVEDAD,
    DETERIORO,
    ALERTA_TEMPRANA;

    /** Revisiones y mantenimientos no quedan abiertos salvo que requieran a otra entidad. */
    public boolean esRutinario() {
        return this == REVISION || this == MANTENIMIENTO;
    }
}