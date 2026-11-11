package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Usuario;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class EmbarcacionServicio {
    private EmbarcacionRepositorio embarcacionRepositorio;

    public Embarcacion buscar(UUID id) {
        return embarcacionRepositorio.findById(id).orElseThrow(
                () -> new NotFoundException("Embarcación no encontrada")
        );
    }

}
