package com.lau.api.dto.motor;

import com.lau.api.dominio.enums.operacion.Embarcaciones;
import java.math.BigDecimal;
import java.util.UUID;

public record MotorResponse(
        UUID id,
        EmbarcacionResumen embarcacion,
        String marca,
        String modelo,
        String serial,
        int potenciaHp,
        BigDecimal horometroActual,
        int intervaloServicioHoras) {

    public record EmbarcacionResumen(UUID id, Embarcaciones nombre, String patente) {
    }


}
