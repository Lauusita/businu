package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.Embarcaderos;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

public record ParadaRequest(
        @NotNull
        Embarcaderos embarcadero,

        @NotNull
        @PastOrPresent
        Instant horaLlegada,

        @NotNull
        @PastOrPresent
        Instant horaSalida
) {
}