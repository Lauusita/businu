package com.lau.api.dto.infraestructura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CerrarRegistroRequest(
        @PastOrPresent Instant fecha,
        @NotBlank @Size(max = 2000) String observacion) {
}