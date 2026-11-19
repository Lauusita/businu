package com.lau.api.dto.operacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FinalizarOperacionRequest(
        @NotNull(message = "La hora de finalización es obligatoria")
        @PastOrPresent
        Instant fechaHoraFin,

        /** Campo heredado (una lectura sin motor). Se recomienda lecturasHorometro. */
        @PositiveOrZero
        @Digits(integer = 9, fraction = 1)
        BigDecimal horometroFinal,

        /** Horómetro de cada motor al llegar (R37). Obligatorio: una lectura por motor. */
        @NotEmpty(message = "Registre la lectura del horómetro de cada motor")
        List<@Valid LecturaHorometroRequest> lecturasHorometro
) {
}
