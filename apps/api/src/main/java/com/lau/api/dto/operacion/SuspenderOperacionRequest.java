package com.lau.api.dto.operacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record SuspenderOperacionRequest(
        @NotBlank @Size(max = 1000) String motivo,
        @PastOrPresent Instant momento) {
}