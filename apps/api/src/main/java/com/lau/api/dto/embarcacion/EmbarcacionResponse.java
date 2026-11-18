package com.lau.api.dto.embarcacion;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.EstadoEmbarcacion;
import java.util.UUID;

public record EmbarcacionResponse(
        UUID id,
        Embarcaciones nombre,
        String patente,
        int capacidadPasajeros,
        EstadoEmbarcacion estado) {

    public static EmbarcacionResponse de(Embarcacion e) {
        return new EmbarcacionResponse(e.getId(), e.getNombre(), e.getPatente(),
                e.getCapacidadPasajeros(), e.getEstado());
    }
}