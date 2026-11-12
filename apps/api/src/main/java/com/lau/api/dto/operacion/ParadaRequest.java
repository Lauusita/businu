package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.Embarcaderos;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ParadaRequest(
        @NotNull Embarcaderos embarcadero,
        @NotNull Instant horaLlegada,
        @NotNull Instant horaSalida) {
}