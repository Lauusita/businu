package com.lau.api.dto;

import com.lau.api.dominio.enums.Rol;
import java.util.UUID;

public record LoginResponse(String accessToken, String tokenType, long expiresIn,
                            UUID personalId, String nombre, Rol rol) { }