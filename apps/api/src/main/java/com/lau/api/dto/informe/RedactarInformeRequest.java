package com.lau.api.dto.informe;

import jakarta.validation.constraints.Size;

/** Partes del informe que escribe el supervisor (§4.7). Un campo null no se modifica. */
public record RedactarInformeRequest(
        @Size(max = 20000) String resumenActividades,
        @Size(max = 20000) String conclusiones,
        @Size(max = 20000) String recomendaciones,
        @Size(max = 20000) String otrosDocumentos) {
}
