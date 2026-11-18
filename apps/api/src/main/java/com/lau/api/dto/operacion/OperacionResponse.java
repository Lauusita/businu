package com.lau.api.dto.operacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.*;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.comun.NovedadMecanicaDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperacionResponse(
        UUID id,
        EmbarcacionResumen embarcacion,
        String embarcaderoOrigen,
        String embarcaderoDestino,
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
        boolean sinNovedadesMecanicas,
        List<NovedadMecanicaDTO> condicionesMecanicas,
        String novedades,
        String observaciones,
        String motivoSuspension,
        BigDecimal horometroFinal,
        int totalUsuariosTransportados,
        String registradoPor,
        boolean cargueCombustible,
        SuspensionDTO suspension,             // R48: solo si el viaje fue suspendido
        List<HorometroDTO> horometros,        // R37: lectura de cada motor al zarpar y al terminar
        BigDecimal horasMotor,                // se omite si falta alguna lectura final
        BigDecimal horasNavegacion,           // se omite mientras el viaje está en curso
        List<CargueDTO> cargues               // R16: abastecimientos anteriores al zarpe
) {

    public record SuspensionDTO(CausaSuspension causa, String causaDescripcion, String detalle, String autoridad) { }

    public record HorometroDTO(UUID motorId, String serial, BigDecimal lecturaInicial, BigDecimal lecturaFinal,
                               BigDecimal horas) { }

    public record CargueDTO(UUID abastecimientoId, Instant fechaHora, BigDecimal galones, boolean tanqueLleno) { }

    public record EmbarcacionResumen(UUID id, Embarcaciones nombre, String patente) { }

    public record Tripulante(UUID id, String nombre, Rol rol) { }

    public record ParadaResponse(String embarcadero, Instant horaLlegada, Instant horaSalida) { }
}