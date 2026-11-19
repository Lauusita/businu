package com.lau.api.dto.motor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MotorRequest(
        @NotNull UUID embarcacionId,
        @NotBlank @Size(max = 40) String marca,
        @Size(max = 60) String modelo,
        @NotBlank @Size(max = 60) String serial,
        @Positive int potenciaHp) {
}
