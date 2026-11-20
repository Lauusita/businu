package com.lau.api.dominio.enums.pqrs;

/**
 * Gestión de una PQRS: RECIBIDA → EN_GESTION → RESPONDIDA o TRASLADADA → CERRADA.
 * TRASLADADA: corresponde a otra entidad (§4.7: "dentro del ámbito de competencia del contratista").
 */
public enum EstadoPqrs {
    RECIBIDA,
    EN_GESTION,
    RESPONDIDA,
    TRASLADADA,
    CERRADA
}
