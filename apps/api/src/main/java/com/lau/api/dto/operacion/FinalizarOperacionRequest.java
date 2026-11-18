package com.lau.api.dto.operacion;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record FinalizarOperacionRequest(
        @NotNull(message = "La hora de finalización es obligatoria")
        @PastOrPresent
        Instant fechaHoraFin,

        @PositiveOrZero
        @Digits(integer = 9, fraction = 1)
        BigDecimal horometroFinal
) {
}