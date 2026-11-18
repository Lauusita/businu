package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;
import java.time.Instant;
import java.util.UUID;

/** Versión liviana para listados. */
public record OperacionResumen(
        UUID id,
        Embarcaciones embarcacion,
        String embarcaderoOrigen,
        String embarcaderoDestino,
        Instant fechaHoraInicio,
        Instant fechaHoraFin,
        EstadoOperacion estado,
        int totalUsuariosTransportados) {
}