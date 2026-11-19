package com.lau.api.dto.documento;

import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dominio.enums.personal.TipoDocumentoPersonal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Documentación de una persona (§4.6): documentos, requisitos que faltan y meses con planilla PILA. */
public record DocumentacionPersonaResponse(
        UUID personalId,
        String nombre,
        String numeroIdentificacion,
        Rol rol,
        Set<TipoDocumentoPersonal> faltantes,        // requeridos para el rol sin documento vigente
        List<YearMonth> planillasPila,               // meses con planilla PILA cargada
        List<DocumentoPersonalResponse> documentos) {
}
