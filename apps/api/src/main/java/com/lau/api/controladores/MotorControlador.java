package com.lau.api.controladores;

import com.lau.api.dto.motor.LecturaHorometroRequest;
import com.lau.api.dto.motor.MotorRequest;
import com.lau.api.dto.motor.MotorResponse;
import com.lau.api.servicios.MotorServicio;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/motores")
public class MotorControlador {

    private final MotorServicio motorServicio;

    public MotorControlador(MotorServicio motorServicio) {
        this.motorServicio = motorServicio;
    }

    @GetMapping
    public List<MotorResponse> listar(@RequestParam(required = false) UUID embarcacionId) {
        return embarcacionId == null
                ? motorServicio.listar()
                : motorServicio.listarPorEmbarcacion(embarcacionId);
    }

    @GetMapping("/{id}")
    public MotorResponse obtener(@PathVariable UUID id) {
        return motorServicio.obtener(id);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @PostMapping
    public ResponseEntity<MotorResponse> registrar(@Valid @RequestBody MotorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(motorServicio.registrar(request));
    }

    /** Ajuste manual (p. ej. al registrar el motor o tras un cambio de tablero). El uso normal llega por la bitácora. */
    @PreAuthorize("hasRole('SUPERVISOR')")
    @PatchMapping("/{id}/horometro")
    public MotorResponse registrarLectura(@PathVariable UUID id,
                                          @Valid @RequestBody LecturaHorometroRequest request) {
        return motorServicio.registrarLectura(id, request);
    }
}