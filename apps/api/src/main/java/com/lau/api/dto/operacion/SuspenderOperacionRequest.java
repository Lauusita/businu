package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.operacion.CausaSuspension;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/** Suspensión de un viaje en curso (§7.1.7, R48). */
public record SuspenderOperacionRequest(
        @NotNull CausaSuspension causa,
        @Size(max = 1000) String detalle,                  // obligatorio si causa = OTRA
        @Size(max = 150) String autoridad,                 // obligatoria si causa = INSTRUCCION_AUTORIDAD
        @Size(max = 1000) String motivo,                   // heredado: se usa como detalle si no viene detalle
        @PastOrPresent Instant momento,
        List<@Valid LecturaHorometroRequest> lecturasHorometro) {

    public String detalleEfectivo() {
        return (detalle == null || detalle.isBlank()) ? motivo : detalle;
    }
}
