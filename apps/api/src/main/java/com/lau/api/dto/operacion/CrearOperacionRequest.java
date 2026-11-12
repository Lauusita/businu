package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Datos para abrir un zarpe. Quien lo registra se toma del usuario autenticado. */
public record CrearOperacionRequest(
        @NotNull UUID embarcacionId,
        @NotNull Embarcaderos embarcaderoOrigen,
        @NotNull Embarcaderos embarcaderoDestino,
        @PastOrPresent Instant fechaHoraInicio,                 // opcional: si falta, ahora
        @NotNull EstadoNave estadoNave,
        @NotNull @Valid VerificacionPreviaDTO verificacion,
        @Size(max = 1000) String justificacionVerificacion,
        @NotNull CondicionClimatica condicionClimatica,
        @NotNull Set<CondicionesMecanicas> condicionesMecanicas,
        String detalleCondicionesMecanicas,
        @NotNull NivelRio nivelRio,
        @NotEmpty Set<UUID> tripulacionIds)
{
}
