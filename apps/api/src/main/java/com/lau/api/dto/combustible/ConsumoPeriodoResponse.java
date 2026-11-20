package com.lau.api.dto.combustible;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Consumo entre dos abastecimientos con tanque lleno (R37). Los galones cargados en el segundo tanque
 * lleno (más los cargues parciales intermedios) son los que se consumieron en los viajes del periodo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConsumoPeriodoResponse(
        UUID abastecimientoInicialId,
        UUID abastecimientoFinalId,
        Instant desde,
        Instant hasta,
        BigDecimal galonesConsumidos,
        int viajes,
        BigDecimal horasNavegacion,           // suma de (llegada − zarpe) de los viajes
        BigDecimal horasMotor,                // suma de horómetros; se omite si falta alguna lectura
        boolean horometroCompleto,
        BigDecimal galonesPorViaje,
        BigDecimal galonesPorHoraNavegacion,
        BigDecimal galonesPorHoraMotor) {
}
