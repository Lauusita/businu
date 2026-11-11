package com.lau.api.dto;

import com.lau.api.dominio.enums.Rol;

import java.util.UUID;

public record PersonalAuth(Rol rol, UUID id) { }
