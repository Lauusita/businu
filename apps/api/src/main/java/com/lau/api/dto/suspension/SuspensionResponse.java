package com.lau.api.dto.suspension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.CausaSuspension;
import com.lau.api.dominio.enums.operacion.Embarcaciones;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuspensionResponse(
        UUID id,
        UUID embarcacionId,
        Embarcaciones embarcacion,
        UUID operacionId,                  // solo si interrumpió un viaje
        CausaSuspension causa,
        String causaDescripcion,
        String detalle,
        String autoridad,
        Instant fechaInicio,
        Instant fechaFin,                  // se omite mientras está vigente
        boolean vigente,
        String observacionLevantamiento,
        String registradoPor,
        String levantadaPor) {
}
