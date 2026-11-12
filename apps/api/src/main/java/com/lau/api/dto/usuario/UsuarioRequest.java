package com.lau.api.dto.usuario;

import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.GrupoEtario;
import com.lau.api.dominio.enums.Sexo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Lo que diligencia el taquillero por cada operación. Quién registra se toma del usuario autenticado. */
public record UsuarioRequest(
        @PastOrPresent Instant fechaHora,
        @NotNull UUID operacionId,
        @NotNull Embarcaderos embarcaderoAbordaje,
        @NotNull @Positive Integer usuariosTransportados,
        @NotEmpty Map<Sexo, @NotNull @PositiveOrZero Integer> porSexo,
        @NotEmpty Map<GrupoEtario, @NotNull @PositiveOrZero Integer> porGrupoEtario,
        @NotEmpty Map<Embarcaderos, @NotNull @PositiveOrZero Integer> porDestino) {
}