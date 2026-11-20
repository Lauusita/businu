package com.lau.api.dto.comun;

import com.lau.api.dominio.NovedadMecanica;
import com.lau.api.dominio.enums.operacion.CondicionesMecanicas;
import com.lau.api.dominio.enums.seguimiento.Severidad;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Novedad mecánica en requests y responses de operación y de mantenimiento. */
public record NovedadMecanicaDTO(
        @NotNull String condicion,
        @NotNull Severidad severidad,
        @Size(max = 2000) String detalle) {           // obligatorio si condicion = OTRA

    public NovedadMecanica toDominio() {
        return new NovedadMecanica(CondicionesMecanicas.valueOf(condicion), severidad, detalle);
    }

    public static NovedadMecanicaDTO desde(NovedadMecanica n) {
        return new NovedadMecanicaDTO(n.getCondicion().getDescripcion(), n.getSeveridad(), n.getDetalle());
    }

    /** null o vacío → lista vacía (= sin novedad). */
    public static List<NovedadMecanica> toDominio(List<NovedadMecanicaDTO> lista) {
        return lista == null ? List.of() : lista.stream().map(NovedadMecanicaDTO::toDominio).toList();
    }

    public static List<NovedadMecanicaDTO> desde(List<NovedadMecanica> lista) {
        return lista.stream().map(NovedadMecanicaDTO::desde).toList();
    }
}