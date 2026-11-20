package com.lau.api.dto.comunicacion;

import com.lau.api.dominio.enums.comunicacion.TipoReferenciaFalla;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Comunicación de una falla o novedad por correo (R50). */
public record ComunicacionRequest(
        @NotNull TipoReferenciaFalla referenciaTipo,
        @NotNull UUID referenciaId,
        @Email @Size(max = 150) String destinatario,     // opcional: si falta, el correo del supervisor configurado
        @NotBlank @Size(max = 200) String asunto,
        @NotBlank @Size(max = 5000) String mensaje) {
}
