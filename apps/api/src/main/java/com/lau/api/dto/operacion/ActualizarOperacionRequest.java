package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.operacion.CondicionClimatica;
import com.lau.api.dominio.enums.operacion.NivelRio;
import com.lau.api.dto.comun.NovedadMecanicaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/** PATCH: solo se modifican los campos que vienen (null = sin cambio). */
public record ActualizarOperacionRequest(
        CondicionClimatica condicionClimatica,
        NivelRio nivelRio,
        List<@Valid NovedadMecanicaDTO> condicionesMecanicas,     // null = sin cambio; [] = sin novedad
        @Size(max = 2000) String novedades,
        @Size(max = 2000) String observaciones) {
}