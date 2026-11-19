package com.lau.api.dto.pqrs;

import java.time.Instant;

/** Lo que ve el usuario al radicar: el número para hacer seguimiento. */
public record RadicadoResponse(String radicado, Instant fecha, String mensaje) {
}
