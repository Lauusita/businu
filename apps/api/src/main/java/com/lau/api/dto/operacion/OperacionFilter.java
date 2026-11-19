package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record OperacionFilter(
        Embarcaciones embarcacion,
        Embarcaderos origen,
        Embarcaderos destino,
        EstadoOperacion estado,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
}