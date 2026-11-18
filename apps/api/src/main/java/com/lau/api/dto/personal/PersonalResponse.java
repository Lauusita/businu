package com.lau.api.dto.personal;

import com.lau.api.dominio.enums.Rol;
import com.lau.api.dominio.enums.TipoIdentificacion;

import java.util.UUID;

public record PersonalResponse (
        UUID id,
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion,
        String nombres,
        String apellidos,
        String email,
        Rol rol ) {
}
