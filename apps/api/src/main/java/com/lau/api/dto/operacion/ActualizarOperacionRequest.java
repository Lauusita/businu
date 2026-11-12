package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.CondicionClimatica;
import com.lau.api.dominio.enums.CondicionesMecanicas;
import com.lau.api.dominio.enums.NivelRio;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** PATCH: solo se modifican los campos que vienen (null = sin cambio). */
public record ActualizarOperacionRequest(
        CondicionClimatica condicionClimatica,
        NivelRio nivelRio,
        Set<CondicionesMecanicas> condicionesMecanicas,
        @Size(max = 2000) String detalleCondicionesMecanicas,
        @Size(max = 2000) String novedades,
        @Size(max = 2000) String observaciones) {
}