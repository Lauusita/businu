package com.lau.api.dto.pqrs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pqrs.EstadoPqrs;
import com.lau.api.dominio.enums.pqrs.TipoPqrs;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PqrsResponse(
        UUID id,
        String radicado,
        Instant fechaRadicacion,
        TipoPqrs tipo,
        String descripcion,
        Embarcaderos embarcadero,
        LocalDate fechaHecho,
        String nombre,
        String correo,
        String telefono,
        boolean autorizaDatos,
        EstadoPqrs estado,
        String respuesta,
        String entidadTraslado,
        String observacionInterna,
        Instant fechaRespuesta,
        String gestionadoPor) {
}
