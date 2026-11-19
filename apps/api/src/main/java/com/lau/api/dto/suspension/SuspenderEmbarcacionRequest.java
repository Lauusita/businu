package com.lau.api.dto.suspension;

import com.lau.api.dominio.enums.operacion.CausaSuspension;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Suspensión de una embarcación que no está navegando (§7.1.7, R48). */
public record SuspenderEmbarcacionRequest(
        @NotNull CausaSuspension causa,
        @Size(max = 1000) String detalle,
        @Size(max = 150) String autoridad,
        @PastOrPresent Instant fechaInicio) {     // opcional: si falta, ahora
}
