package com.lau.api.dto.operacion;

import jakarta.validation.constraints.NotNull;

public record VerificacionPreviaDTO(
        @NotNull Boolean aceiteMotor,
        @NotNull Boolean combustible,
        @NotNull Boolean sistemaDireccion,
        @NotNull Boolean bombaAchique,
        @NotNull Boolean lucesNavegacion,
        @NotNull Boolean chalecosSalvavidas,
        @NotNull Boolean radioComunicacion) {
}