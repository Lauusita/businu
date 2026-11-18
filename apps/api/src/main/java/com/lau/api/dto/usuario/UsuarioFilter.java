package com.lau.api.dto.usuario;

import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** Filtros opcionales de GET /pasajeros. Un campo null no filtra. */
public record UsuarioFilter(
        Embarcaciones embarcacion,
        Embarcaderos puertoAbordaje,
        Embarcaderos puertoDestino,
        Integer cantidadMinima,
        Integer cantidadMaxima,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
}