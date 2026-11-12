package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperacionResponse(
        UUID id,
        Long version,
        EmbarcacionResumen embarcacion,
        Embarcaderos embarcaderoOrigen,
        Embarcaderos embarcaderoDestino,
        Instant fechaHoraInicio,
        Instant fechaHoraFin,
        EstadoOperacion estado,
        EstadoNave estadoNave,
        VerificacionPreviaDTO verificacion,
        String justificacionVerificacion,
        CondicionClimatica condicionClimatica,
        NivelRio nivelRio,
        List<Tripulante> tripulacion,
        List<ParadaResponse> paradas,
        Set<CondicionesMecanicas> condicionesMecanicas,
        String detalleCondicionesMecanicas,
        String novedades,
        String observaciones,
        String motivoSuspension,
        BigDecimal horometroFinal,
        int totalUsuariosTransportados,
        String registradoPor) {

    public record EmbarcacionResumen(UUID id, Embarcaciones nombre, String patente) { }

    public record Tripulante(UUID id, String nombre, Rol rol) { }

    public record ParadaResponse(Embarcaderos embarcadero, Instant horaLlegada, Instant horaSalida) { }
}