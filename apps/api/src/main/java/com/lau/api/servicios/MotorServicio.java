package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Motor;
import com.lau.api.dto.motor.LecturaHorometroRequest;
import com.lau.api.dto.motor.MotorRequest;
import com.lau.api.dto.motor.MotorResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.MotorMapper;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.MotorRepositorio;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MotorServicio {

    private static final int MAX_MOTORES_POR_EMBARCACION = 2;

    private final MotorRepositorio repositorio;
    private final EmbarcacionRepositorio embarcaciones;
    private final MotorMapper mapper;

    public MotorServicio(MotorRepositorio repositorio, EmbarcacionRepositorio embarcaciones, MotorMapper mapper) {
        this.repositorio = repositorio;
        this.embarcaciones = embarcaciones;
        this.mapper = mapper;
    }

    public List<MotorResponse> listar() {
        return mapper.toResponse(repositorio.findAllByOrderBySerialAsc());
    }

    public MotorResponse obtener(UUID id) {
        return mapper.toResponse(buscarPorId(id));
    }

    /** Cada embarcación tiene dos motores, por eso devuelve una lista. */
    public List<MotorResponse> listarPorEmbarcacion(UUID embarcacionId) {
        if (!embarcaciones.existsById(embarcacionId)) {
            throw new NotFoundException("Embarcación no encontrada");
        }
        return mapper.toResponse(repositorio.findByEmbarcacionIdOrderBySerialAsc(embarcacionId));
    }

    @Transactional
    public MotorResponse registrar(MotorRequest r) {
        Embarcacion embarcacion = embarcaciones.findById(r.embarcacionId())
                .orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));

        String serial = r.serial().trim();
        if (repositorio.existsBySerial(serial)) {
            throw new BusinessException("Ya existe un motor con el serial " + serial);
        }
        if (repositorio.countByEmbarcacionId(embarcacion.getId()) >= MAX_MOTORES_POR_EMBARCACION) {
            throw new BusinessException("La embarcación " + embarcacion.getNombre()
                    + " ya tiene " + MAX_MOTORES_POR_EMBARCACION + " motores registrados");
        }

        Motor motor = embarcacion.agregarMotor(r.marca().trim(), r.modelo(), serial, r.potenciaHp());
        return mapper.toResponse(repositorio.save(motor));
    }

    @Transactional
    public MotorResponse registrarLectura(UUID id, LecturaHorometroRequest r) {
        Motor motor = buscarPorId(id);
        motor.registrarLectura(r.lectura());
        return mapper.toResponse(motor);
    }

    /** Para uso interno de otros servicios (mantenimiento, bitácora). */
    public Motor buscarPorId(UUID id) {
        return repositorio.findById(id).orElseThrow(() -> new NotFoundException("Motor no encontrado"));
    }
}