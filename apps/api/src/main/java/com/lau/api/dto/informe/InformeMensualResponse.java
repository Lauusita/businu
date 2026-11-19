package com.lau.api.dto.informe;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.EstadoInforme;
import java.time.Instant;
import java.time.YearMonth;

/** Estado del informe, el texto del supervisor y las cifras (en vivo si es borrador, congeladas si no). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InformeMensualResponse(
        YearMonth periodo,
        EstadoInforme estado,          // BORRADOR aunque todavía no se haya guardado texto
        String resumenActividades,
        String conclusiones,
        String recomendaciones,
        String otrosDocumentos,
        Instant fechaCierre,
        String cerradoPor,
        Instant fechaEntrega,
        String entregadoPor,
        String huellaDatos,
        InformeMensualDatos datos) {
}
