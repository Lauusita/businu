package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.operacion.CondicionClimatica;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.operacion.EstadoNave;
import com.lau.api.dominio.enums.operacion.NivelRio;
import com.lau.api.dto.comun.NovedadMecanicaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
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
        List<@Valid NovedadMecanicaDTO> condicionesMecanicas,     // vacía o ausente = sin novedad
        @NotNull NivelRio nivelRio,
        @NotEmpty Set<UUID> tripulacionIds,
        List<@Valid LecturaHorometroRequest> lecturasHorometro) {   // opcional: horómetro de cada motor al zarpar
}