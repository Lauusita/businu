package com.lau.api.dto.usuario;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.Sexo;

public record UsuarioFilter (
    Embarcaciones embarcacion,
    Embarcaderos puertoAbordaje,
    Embarcaderos puertoDestino,
    int cantidadTransportada,
    String fecha
) {
}
