package com.lau.api.dto.mantenimiento;

import com.lau.api.dominio.enums.CondicionesMecanicas;
import com.lau.api.dominio.enums.mantenimiento.*;
import com.lau.api.dominio.enums.seguimiento.Severidad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Registro de una intervención (§4.3). Quién la registra se toma del usuario autenticado.
 * Listas vacías o ausentes en repuestos / novedades = "No aplica / Ninguno".
 */
public record CrearMantenimientoRequest(
        @NotNull UUID embarcacionId,
        UUID motorId,                                         // obligatorio si sistema = MOTOR
        @NotNull TipoMantenimiento tipo,
        @NotNull SistemaIntervenido sistema,
        @NotNull @PastOrPresent Instant fechaIntervencion,
        @NotBlank @Size(max = 4000) String descripcionActividades,
        @Size(max = 150) String ejecutadoPor,
        @NotNull UUID responsableId,                          // operador de embarcación asignado
        @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal costoManoObra,
        List<@Valid Repuesto> repuestos,
        List<@Valid Novedad> novedades,
        List<@Valid Recomendacion> recomendaciones,
        @Future Instant proximaIntervencion,
        @Size(max = 300) String condicionProximaIntervencion,
        List<UUID> soporteIds) {                              // devueltos por POST /adjuntos?carpeta=mantenimiento

    public record Repuesto(
            @NotNull RepuestoInsumo item,
            @Size(max = 200) String descripcion,              // obligatoria si item = OTRO
            @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal cantidad,
            @Size(max = 20) String unidad,
            @PositiveOrZero @Digits(integer = 12, fraction = 2) BigDecimal costo) {
    }

    public record Novedad(
            @NotNull CondicionesMecanicas condicion,
            @NotNull Severidad severidad,
            @Size(max = 2000) String detalle) {               // obligatorio si condicion = OTRA
    }

    public record Recomendacion(
            @NotNull TipoRecomendacion tipo,
            @NotBlank @Size(max = 2000) String descripcion) {
    }
}