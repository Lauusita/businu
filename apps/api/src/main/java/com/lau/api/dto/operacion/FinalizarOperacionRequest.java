package com.lau.api.dto.operacion;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record FinalizarOperacionRequest(
        @PastOrPresent @NotNull(message = "la hora de finalización es obligatoria") Instant fechaHoraFin,// opcional: si falta, ahora
        @PositiveOrZero @Digits(integer = 9, fraction = 1) BigDecimal horometroFinal) {
}