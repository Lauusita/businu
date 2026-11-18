package com.lau.api.dto.mantenimiento;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.CondicionesMecanicas;
import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.mantenimiento.*;
import com.lau.api.dominio.enums.seguimiento.Severidad;
import com.lau.api.dto.combustible.AbastecimientoResponse.SoporteDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MantenimientoResponse(
        UUID id,
        UUID embarcacionId,
        Embarcaciones embarcacion,
        MotorResumen motor,                    // solo si se intervino un motor
        TipoMantenimiento tipo,
        SistemaIntervenido sistema,
        Instant fechaIntervencion,
        String descripcionActividades,
        String ejecutadoPor,
        String responsable,
        String registradoPor,
        boolean sinRepuestos,
        List<Repuesto> repuestos,
        boolean sinNovedades,
        List<Novedad> novedades,
        List<Recomendacion> recomendaciones,
        Instant proximaIntervencion,           // solo si se estimó una fecha
        String condicionProximaIntervencion,
        BigDecimal costoManoObra,
        BigDecimal costoRepuestos,
        BigDecimal costoTotal,
        List<SoporteDTO> soportes) {

    public record MotorResumen(UUID id, String serial, String modelo) { }

    public record Repuesto(RepuestoInsumo item, String descripcion, BigDecimal cantidad, String unidad,
                           BigDecimal costo) { }

    public record Novedad(CondicionesMecanicas condicion, Severidad severidad, String detalle) { }

    public record Recomendacion(TipoRecomendacion tipo, String descripcion) { }
}