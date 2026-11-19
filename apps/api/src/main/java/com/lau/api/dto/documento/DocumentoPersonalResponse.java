package com.lau.api.dto.documento;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.personal.TipoDocumentoPersonal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentoPersonalResponse(
        UUID id,
        TipoDocumentoPersonal tipo,
        String descripcionTipo,
        YearMonth periodo,             // solo planilla PILA
        boolean vigente,
        String nombreArchivo,
        long tamanoBytes,
        String sha256,
        String observaciones,
        Instant cargadoEn,
        String registradoPor) {
}
