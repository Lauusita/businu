package com.lau.api.dto.login;

import jakarta.validation.constraints.NotBlank;

public record SolicitudRecuperacionRequest(
        @NotBlank(message = "El número de identificación es obligatorio") String numeroIdentificacion) { }
