package com.lau.api.mappers;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Mantenimiento;
import com.lau.api.dominio.Motor;
import com.lau.api.dominio.NovedadMantenimiento;
import com.lau.api.dominio.RecomendacionMantenimiento;
import com.lau.api.dominio.RepuestoUtilizado;
import com.lau.api.dto.combustible.AbastecimientoResponse.SoporteDTO;
import com.lau.api.dto.embarcacion.EmbarcacionResponse;
import com.lau.api.dto.mantenimiento.CrearMantenimientoRequest;
import com.lau.api.dto.mantenimiento.MantenimientoResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MantenimientoMapper {

    // ---- Request → dominio ----

    public List<RepuestoUtilizado> toRepuestos(List<CrearMantenimientoRequest.Repuesto> lista) {
        return lista == null ? List.of() : lista.stream()
                .map(r -> new RepuestoUtilizado(r.item(), r.descripcion(), r.cantidad(), r.unidad(), r.costo()))
                .toList();
    }

    public List<NovedadMantenimiento> toNovedades(List<CrearMantenimientoRequest.Novedad> lista) {
        return lista == null ? List.of() : lista.stream()
                .map(n -> new NovedadMantenimiento(n.condicion(), n.severidad(), n.detalle()))
                .toList();
    }

    public List<RecomendacionMantenimiento> toRecomendaciones(List<CrearMantenimientoRequest.Recomendacion> lista) {
        return lista == null ? List.of() : lista.stream()
                .map(r -> new RecomendacionMantenimiento(r.tipo(), r.descripcion()))
                .toList();
    }

    // ---- Dominio → response (llamar dentro de la transacción: hay relaciones LAZY) ----

    public MantenimientoResponse toResponse(Mantenimiento m) {
        Motor motor = m.getMotor();
        return new MantenimientoResponse(
                m.getId(),
                m.getEmbarcacion().getId(),
                m.getEmbarcacion().getNombre(),
                motor == null ? null : new MantenimientoResponse.MotorResumen(motor.getId(), motor.getSerial(),
                        motor.getModelo()),
                m.getTipo(),
                m.getSistema(),
                m.getFechaIntervencion(),
                m.getDescripcionActividades(),
                m.getEjecutadoPor(),
                m.getResponsable().nombreCompleto(),
                m.getRegistradoPor().nombreCompleto(),
                m.isSinRepuestos(),
                m.getRepuestos().stream()
                        .map(r -> new MantenimientoResponse.Repuesto(r.getItem(), r.getDescripcion(), r.getCantidad(),
                                r.getUnidad(), r.getCosto()))
                        .toList(),
                m.isSinNovedades(),
                m.getNovedades().stream()
                        .map(n -> new MantenimientoResponse.Novedad(n.getCondicion(), n.getSeveridad(), n.getDetalle()))
                        .toList(),
                m.getRecomendaciones().stream()
                        .map(r -> new MantenimientoResponse.Recomendacion(r.getTipo(), r.getDescripcion()))
                        .toList(),
                m.getProximaIntervencion(),
                m.getCondicionProximaIntervencion(),
                m.getCostoManoObra(),
                m.costoRepuestos(),
                m.costoTotal(),
                m.getSoportes().stream()
                        .map(a -> new SoporteDTO(a.getId(), a.getNombreOriginal(), a.getTamanoBytes(), a.getSha256()))
                        .toList());
    }

    public EmbarcacionResponse toEmbarcacion(Embarcacion e) {
        return new EmbarcacionResponse(e.getId(), e.getNombre(), e.getPatente(), e.getCapacidadPasajeros(),
                e.getEstado(),
                e.getMotores().stream()
                        .map(m -> new EmbarcacionResponse.MotorDTO(m.getId(), m.getMarca(), m.getModelo(),
                                m.getSerial(), m.getPotenciaHp()))
                        .toList());
    }
}