package com.lau.api.dto.infraestructura;

import com.lau.api.dominio.enums.seguimiento.EstadoActivo;

import java.time.Instant;

/** Tablero: estado actual de cada embarcadero y ayuda. */
public record EstadoInfraestructuraResponse(
        String tipo,                         // "EMBARCADERO" o "AYUDA"
        String identificador,                // RANCHO_GRANDE, B-03...
        EstadoActivo estadoActual,           // del último registro; OPERATIVO si nunca se registró nada
        Instant ultimaRevision,
        long registrosAbiertos) {
}