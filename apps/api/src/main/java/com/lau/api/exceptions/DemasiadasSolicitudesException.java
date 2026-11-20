package com.lau.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class DemasiadasSolicitudesException extends RuntimeException {
    public DemasiadasSolicitudesException(String mensaje) {
        super(mensaje);
    }
}
