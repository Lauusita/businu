package com.lau.api.dto.pqrs;

import com.lau.api.dominio.enums.pqrs.EstadoPqrs;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GestionarPqrsRequest(
        @NotNull EstadoPqrs estado,
        @Size(max = 5000) String respuesta,           // obligatoria para RESPONDIDA
        @Size(max = 200) String entidadTraslado,      // obligatoria para TRASLADADA
        @Size(max = 2000) String observacionInterna) {
}
