package com.lau.api.dto.personal;

import com.lau.api.dominio.enums.Rol;
import com.lau.api.dominio.enums.TipoIdentificacion;

public record PersonalResponse (
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion,
        String nombres,
        String apellidos,
        String email,
        Rol rol ) {
}
