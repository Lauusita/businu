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

        /** Horómetro de cada motor al llegar (R37). Opcional mientras no se defina como obligatorio. */
        List<@Valid LecturaHorometroRequest> lecturasHorometro
) {
}
