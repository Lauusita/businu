package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmbarcacionServicio {
    private final EmbarcacionRepositorio embarcacionRepositorio;

    public EmbarcacionServicio(EmbarcacionRepositorio embarcacionRepositorio) {
        this.embarcacionRepositorio = embarcacionRepositorio;
    }

    public Embarcacion buscar(UUID id) {
        return embarcacionRepositorio.findById(id).orElseThrow(
                () -> new NotFoundException("Embarcación no encontrada")
        );
    }

}
