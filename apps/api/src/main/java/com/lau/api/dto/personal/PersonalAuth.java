package com.lau.api.dto.personal;

import com.lau.api.dominio.enums.Rol;

import java.util.UUID;

public record PersonalAuth(Rol rol, UUID id) { }
