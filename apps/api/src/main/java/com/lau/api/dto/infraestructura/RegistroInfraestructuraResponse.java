package com.lau.api.dto.infraestructura;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.*;

import java.time.Instant;
import com.lau.api.dto.combustible.AbastecimientoResponse.SoporteDTO;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegistroInfraestructuraResponse(
        UUID id,
        Instant fechaHora,
        Embarcaderos embarcadero,
        AyudaResumen ayuda,
        TipoRegistroInfraestructura tipo,
        ComponenteInfraestructura componente,
        EstadoActivo estadoObservado,
        Severidad severidad,
        Set<AspectoAfectado> aspectosAfectados,
        String descripcion,
        String accionesRealizadas,
        boolean requiereIntervencionExterna,
        EntidadCompetente entidadCompetente,
        EstadoSeguimiento estado,
        boolean pendienteDeComunicar,
        Long horasSinComunicar,              // solo si está pendiente: alerta de "oportunamente"
        ComunicacionDTO comunicacion,
        CierreDTO cierre,
        String registradoPor,
        List<SoporteDTO> evidencias) {       // R34: PDF almacenados (las fotos se convierten a PDF)

    public record AyudaResumen(UUID id, String codigo, EstructuraAyuda estructura,
                               FuncionMarca funcion, boolean luminosa) { }

    public record ComunicacionDTO(Instant fecha, MedioComunicacion medio, String referencia, String comunicadoPor) { }

    public record CierreDTO(Instant fecha, String observacion, String cerradoPor) { }
}