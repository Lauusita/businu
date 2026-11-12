package com.lau.api.dto.operacion;

import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.EstadoOperacion;
import java.time.Instant;
import java.util.UUID;

/** Versión liviana para listados. */
public record OperacionResumen(
        UUID id,
        Embarcaciones embarcacion,
        Embarcaderos embarcaderoOrigen,
        Embarcaderos embarcaderoDestino,
        Instant fechaHoraInicio,
        Instant fechaHoraFin,
        EstadoOperacion estado,
        int totalUsuariosTransportados) {
}