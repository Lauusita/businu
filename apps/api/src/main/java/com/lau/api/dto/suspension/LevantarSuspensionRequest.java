package com.lau.api.dto.suspension;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record LevantarSuspensionRequest(
        @NotBlank @Size(max = 1000) String observacion,   // por qué se restablece la operación
        @PastOrPresent Instant fecha) {                  // opcional: si falta, ahora
}
