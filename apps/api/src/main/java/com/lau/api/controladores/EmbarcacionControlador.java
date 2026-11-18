package com.lau.api.controladores;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dto.embarcacion.EmbarcacionResponse;
import com.lau.api.servicios.EmbarcacionServicio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/embarcaciones")
public class EmbarcacionControlador {
    private final EmbarcacionServicio embarcacionServicio;

    public EmbarcacionControlador(EmbarcacionServicio embarcacionServicio) {
        this.embarcacionServicio = embarcacionServicio;
    }

    @GetMapping
    public List<EmbarcacionResponse> obtenerTodas() {
        return embarcacionServicio.obtenerTodas();
    }

    @GetMapping("{id}")
    public Embarcacion obtenerPorId(@PathVariable UUID id) {
        return embarcacionServicio.buscar(id);
    }
}
