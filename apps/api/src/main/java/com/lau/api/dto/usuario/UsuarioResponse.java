package com.lau.api.dto.usuario;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pasajeros.GrupoEtario;
import com.lau.api.dominio.enums.pasajeros.Sexo;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)

public record UsuarioResponse(
        UUID id,
        Instant fechaHora,
        EmbarcacionResumen embarcacion,
        EmbarcaderoResumen embarcaderoAbordaje,
        Map<Embarcaderos, Integer> porDestino,
        int usuariosTransportados,
        Map<Sexo, Integer> porSexo,
        Map<GrupoEtario, Integer> porGrupoEtario,
        String registradoPor,
        Instant creadoEn) {

    public record EmbarcacionResumen(UUID id, Embarcaciones nombre, String patente) {
    }

    /** codigo = valor del enum (para enviar de vuelta); nombre = texto para mostrar. */
    public record EmbarcaderoResumen(Embarcaderos codigo, String nombre) {
        public static EmbarcaderoResumen de(Embarcaderos e) {
            return new EmbarcaderoResumen(e, e.getDescripcion().replace("_", " "));
        }
    }
}