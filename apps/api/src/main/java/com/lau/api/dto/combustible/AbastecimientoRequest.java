package com.lau.api.dto.combustible;

import com.lau.api.dominio.enums.abastecimiento.TipoCombustible;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AbastecimientoRequest(
        @PastOrPresent Instant fechaHora,
        @NotNull UUID embarcacionId,
        @NotNull TipoCombustible tipoCombustible,
        @NotNull @Positive @Digits(integer = 6, fraction = 2) BigDecimal galones,
        @NotNull @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal valorTotal,
        @NotNull Boolean tanqueLleno,
        @NotNull UUID soporteId,                // devuelto por POST /adjuntos
        @Size(max = 2000) String observaciones) {
}