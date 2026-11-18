package com.lau.api.mappers;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dto.embarcacion.EmbarcacionResponse;
import org.springframework.stereotype.Component;

@Component
public class EmbarcacionMapper {
    public EmbarcacionResponse toResponse(Embarcacion e) {
        return new EmbarcacionResponse(
                e.getId(),
                e.getNombre(),
                e.getPatente(),
                e.getCapacidadPasajeros(),
                e.getEstado()
        );
    }
}
