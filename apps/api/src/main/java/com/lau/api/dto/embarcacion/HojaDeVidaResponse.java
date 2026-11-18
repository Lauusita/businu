package com.lau.api.dto.embarcacion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dto.mantenimiento.MantenimientoResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Hoja de vida de una embarcación (§4.3): datos, motores, intervenciones y próxima intervención. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HojaDeVidaResponse(
        EmbarcacionResponse embarcacion,
        int totalIntervenciones,
        long preventivos,
        long correctivos,
        Instant ultimaIntervencion,             // se omite si no hay intervenciones
        Instant proximaIntervencion,            // la más cercana pendiente; se omite si no hay
        BigDecimal costoTotalMantenimiento,
        List<MantenimientoResponse> intervenciones) {
}