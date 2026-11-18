package com.lau.api.dto.embarcacion;

import com.lau.api.dominio.enums.Embarcaciones;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record EmbarcacionRequest(
        @NotBlank @Size(max = 40) String patente,
        @NotNull Embarcaciones nombre,
        @Positive int capacidadPasajeros,
        @Size(max = 255) String nic,
        @Size(max = 100) String constructor,
        @Min(1900) @Max(2100) int anioConstruccion,
        @Valid List<MotorRequest> motores) {

    public record MotorRequest(
            @NotBlank String marca,
            @NotBlank String modelo,
            @NotBlank String serial,
            @Positive int potenciaHp) {
    }
}