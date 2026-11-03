package com.lau.api.services;

import com.lau.api.domain.Motor;
import com.lau.api.dto.ProyeccionServicio;
import com.lau.api.repositories.MantenimientoMotorRepository;
import com.lau.api.repositories.MotorRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta la proyección del próximo servicio de 100 h para un motor. */
@Service
@Transactional(readOnly = true)
public class ProximoServicioService {

    private final MotorRepository motores;
    private final MantenimientoMotorRepository mantenimientosMotor;
    private final CalculadoraProximoServicio calculadora;
    private final Clock reloj;

    public ProximoServicioService(MotorRepository motores, MantenimientoMotorRepository mantenimientosMotor,
                                  CalculadoraProximoServicio calculadora, Clock reloj) {
        this.motores = motores;
        this.mantenimientosMotor = mantenimientosMotor;
        this.calculadora = calculadora;
        this.reloj = reloj;
    }

    public ProyeccionServicio proyectar(UUID motorId) {
        Motor motor = motores.findById(motorId)
                .orElseThrow(() -> new IllegalArgumentException("Motor no encontrado: " + motorId));
        return mantenimientosMotor.findFirstByMotorIdAndServicioPeriodicoTrueOrderByHorometroDesc(motorId)
                .map(ultimo -> calculadora.proyectar(
                        ultimo.getProximoHorometro(),
                        motor.getHorometroActual(),
                        motor.getEmbarcacion().getHorasOperacionDia(),
                        ultimo.getMantenimiento().getFechaIntervencion(),
                        LocalDate.now(reloj)))
                .orElseGet(() -> ProyeccionServicio.sinServicio(motor.getHorometroActual()));
    }
}
