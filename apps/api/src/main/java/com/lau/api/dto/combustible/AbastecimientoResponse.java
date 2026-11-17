package com.lau.api.dto.combustible;

import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.TipoCombustible;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Respuesta de un registro de combustible (§4.5). */
public record AbastecimientoResponse(
        UUID id,
        Instant fechaHora,
        UUID embarcacionId,
        Embarcaciones embarcacion,
        TipoCombustible tipoCombustible,
        BigDecimal galones,
        BigDecimal valorTotal,
        BigDecimal precioPorGalon,
        boolean tanqueLleno,
        SoporteDTO soporte,
        String observaciones) {

    public record SoporteDTO(
            UUID id,
            String nombreOriginal,
            long tamanoBytes,
            String sha256) {
    }
}