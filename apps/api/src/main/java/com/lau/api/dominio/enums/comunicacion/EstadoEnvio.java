package com.lau.api.dominio.enums.comunicacion;

/** Resultado del envío del correo. ENVIADO solo cuando el servidor SMTP aceptó el mensaje. */
public enum EstadoEnvio {
    PENDIENTE,
    ENVIADO,
    FALLIDO
}
