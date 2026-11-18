package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Mantenimiento;
import com.lau.api.dominio.Motor;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.mantenimiento.CrearMantenimientoRequest;
import com.lau.api.dto.mantenimiento.MantenimientoResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.MantenimientoMapper;
import com.lau.api.repositorio.MantenimientoRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro y consulta de intervenciones de mantenimiento (§4.3). */
@Service
@Transactional(readOnly = true)
public class MantenimientoServicio {

    private final MantenimientoRepositorio mantenimientos;
    private final EmbarcacionServicio embarcaciones;
    private final PersonalRepositorio personal;
    private final OperacionRepositorio operaciones;
    private final AdjuntoServicio adjuntoServicio;
    private final MantenimientoMapper mapper;

    public MantenimientoServicio(MantenimientoRepositorio mantenimientos, EmbarcacionServicio embarcaciones,
                                 PersonalRepositorio personal, OperacionRepositorio operaciones,
                                 AdjuntoServicio adjuntoServicio, MantenimientoMapper mapper) {
        this.mantenimientos = mantenimientos;
        this.embarcaciones = embarcaciones;
        this.personal = personal;
        this.operaciones = operaciones;
        this.adjuntoServicio = adjuntoServicio;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- Registro

    @Transactional
    public MantenimientoResponse registrar(CrearMantenimientoRequest r, UUID usuarioId) {
        Personal registrador = buscarPersonal(usuarioId);
        if (registrador.getRol() != Rol.OPERADOR_EMBARCACION && registrador.getRol() != Rol.SUPERVISOR) {
            throw new BusinessException("Solo operadores de embarcación y supervisores registran mantenimientos");
        }

        Embarcacion embarcacion = embarcaciones.buscar(r.embarcacionId());
        if (operaciones.existsByEmbarcacionIdAndEstado(embarcacion.getId(), EstadoOperacion.EN_CURSO)) {
            throw new BusinessException("La embarcación tiene una operación en curso: "
                    + "finalícela o suspéndala antes de registrar el mantenimiento");
        }
        Motor motor = r.motorId() == null ? null : embarcacion.getMotores().stream()
                .filter(m -> m.getId().equals(r.motorId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("El motor indicado no pertenece a " + embarcacion.getNombre()));

        Mantenimiento m = new Mantenimiento(embarcacion, motor, r.tipo(), r.sistema(), r.fechaIntervencion(),
                r.descripcionActividades(), r.ejecutadoPor(), buscarPersonal(r.responsableId()), registrador,
                r.costoManoObra());
        m.registrarRepuestos(mapper.toRepuestos(r.repuestos()));
        m.registrarNovedades(mapper.toNovedades(r.novedades()));
        m.registrarRecomendaciones(mapper.toRecomendaciones(r.recomendaciones()));
        m.programarProximaIntervencion(r.proximaIntervencion(), r.condicionProximaIntervencion());

        if (r.soporteIds() != null) {
            r.soporteIds().forEach(id -> m.agregarSoporte(adjuntoServicio.vincular(id, registrador)));
        }
        return mapper.toResponse(mantenimientos.save(m));
    }

    // ---------------------------------------------------------------- Consultas

    public MantenimientoResponse obtener(UUID id) {
        return mapper.toResponse(mantenimientos.findById(id)
                .orElseThrow(() -> new NotFoundException("Mantenimiento no encontrado")));
    }

    // ---------------------------------------------------------------- Auxiliares

    private Personal buscarPersonal(UUID id) {
        return personal.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }
}