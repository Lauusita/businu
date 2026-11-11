package ar.edu.unsam.phm.booklibre.exceptions


class TokenExpiredException(mensaje: String = "Token vencido") : RuntimeException(mensaje)