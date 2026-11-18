package com.lau.api.mappers;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Motor;
import com.lau.api.dto.motor.MotorResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MotorMapper {

    public MotorResponse toResponse(Motor m) {
        Embarcacion e = m.getEmbarcacion();
        return new MotorResponse(
                m.getId(),
                new MotorResponse.EmbarcacionResumen(e.getId(), e.getNombre(), e.getPatente()),
                m.getMarca(),
                m.getModelo(),
                m.getSerial(),
                m.getPotenciaHp(),
                m.getHorometroActual(),
                m.getIntervaloServicioHoras());
    }

    public List<MotorResponse> toResponse(List<Motor> motores) {
        return motores.stream().map(this::toResponse).toList();
    }
}
