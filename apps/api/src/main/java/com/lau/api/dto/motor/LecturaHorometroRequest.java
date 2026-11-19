package com.lau.api.dto.motor;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record LecturaHorometroRequest(
        @NotNull @PositiveOrZero @Digits(integer = 9, fraction = 1) BigDecimal lectura) {
}
