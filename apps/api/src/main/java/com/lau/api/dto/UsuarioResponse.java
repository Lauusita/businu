package com.lau.api.dto;

import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.GrupoEtario;
import com.lau.api.dominio.enums.Sexo;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        Instant fechaHora,
        EmbarcacionResumen embarcacion,
        EmbarcaderoResumen embarcaderoOrigen,
        EmbarcaderoResumen embarcaderoDestino,
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
            return new EmbarcaderoResumen(e, e.name());
        }
    }
}