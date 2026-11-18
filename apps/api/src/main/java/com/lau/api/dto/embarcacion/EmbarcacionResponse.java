package com.lau.api.dto.embarcacion;

import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.EstadoEmbarcacion;
import java.util.List;
import java.util.UUID;

public record EmbarcacionResponse(
        UUID id,
        Embarcaciones nombre,
        String patente,
        int capacidadPasajeros,
        EstadoEmbarcacion estado,
        List<MotorDTO> motores) {

    public record MotorDTO(UUID id, String marca, String modelo, String serial, int potenciaHp) { }
}