package com.lau.api.dto.infraestructura;

import com.lau.api.dominio.enums.seguimiento.MedioComunicacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ComunicarSupervisorRequest(
        @PastOrPresent Instant fecha,
        @NotNull MedioComunicacion medio,
        @Size(max = 200) String referencia) {
}