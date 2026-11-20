package com.lau.api.dto.auditoria;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Una modificación de un registro: cuándo, quién, qué tipo de cambio y qué campos cambiaron (R5). */
public record RevisionResponse(
        long revision,
        Instant fecha,
        Accion accion,
        UUID autorId,                     // null si el cambio lo hizo el sistema
        String autor,                     // "Sistema" si no hubo usuario autenticado
        List<String> camposModificados) { // vacío en la creación y en cambios solo de listas internas

    public enum Accion { CREACION, MODIFICACION, ELIMINACION }
}
