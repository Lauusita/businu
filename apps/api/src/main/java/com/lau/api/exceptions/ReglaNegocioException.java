package com.lau.api.exceptions;

/** Se lanza cuando un registro viola una regla del anexo técnico o del modelo. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
