package com.lau.api.dto.infraestructura;

import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Exactamente uno de embarcadero / ayudaId. */
public record CrearRegistroInfraestructuraRequest(
        @PastOrPresent Instant fechaHora,
        Embarcaderos embarcadero,
        UUID ayudaId,
        @NotNull TipoRegistroInfraestructura tipo,
        @NotNull ComponenteInfraestructura componente,
        @NotNull EstadoActivo estadoObservado,
        @NotNull Severidad severidad,
        Set<AspectoAfectado> aspectosAfectados,
        @NotBlank @Size(max = 2000) String descripcion,
        @Size(max = 2000) String accionesRealizadas,
        @NotNull Boolean requiereIntervencionExterna,
        EntidadCompetente entidadCompetente
) {}