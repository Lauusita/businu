package com.lau.api.dto.operacion;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/** Lectura del horómetro de un motor (R37). */
public record LecturaHorometroRequest(
        @NotNull UUID motorId,
        @NotNull @PositiveOrZero @Digits(integer = 9, fraction = 1) BigDecimal lectura) {
}
