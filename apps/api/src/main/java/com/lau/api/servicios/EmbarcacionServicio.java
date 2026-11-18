package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dto.embarcacion.EmbarcacionResponse;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.EmbarcacionMapper;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmbarcacionServicio {
    private final EmbarcacionRepositorio embarcacionRepositorio;
    private final EmbarcacionMapper mapper;

    public EmbarcacionServicio(EmbarcacionRepositorio embarcacionRepositorio, EmbarcacionMapper mapper) {
        this.embarcacionRepositorio = embarcacionRepositorio;
        this.mapper = mapper;
    }

    public Embarcacion buscar(UUID id) {
        return embarcacionRepositorio.findById(id).orElseThrow(
                () -> new NotFoundException("Embarcación no encontrada")
        );
    }

    public Embarcacion obtenerPorNombre(Embarcaciones nombre) {
        return embarcacionRepositorio.findByNombre(nombre);
    }
    public List<EmbarcacionResponse> obtenerTodas() {
        return embarcacionRepositorio.findAll().stream().map(mapper::toResponse).toList();
    }

}
