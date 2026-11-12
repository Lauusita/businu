package com.lau.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String numeroIdentificacion, @NotBlank String contrasena) { }