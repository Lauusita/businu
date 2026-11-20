package com.lau.api.dto.comunicacion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.comunicacion.EstadoEnvio;
import com.lau.api.dominio.enums.comunicacion.TipoReferenciaFalla;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComunicacionResponse(
        UUID id,
        TipoReferenciaFalla referenciaTipo,
        UUID referenciaId,
        String destinatario,
        String asunto,
        EstadoEnvio estado,
        Instant fechaRegistro,
        Instant fechaEnvio,          // solo si ENVIADO
        String messageId,            // solo si ENVIADO
        String error,                // solo si FALLIDO
        int intentos,
        String registradoPor) {
}
