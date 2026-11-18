package com.lau.api.servicios;

import com.lau.api.config.HorarioOperacion;
import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.comun.NovedadMecanicaDTO;
import com.lau.api.dto.operacion.*;
import com.lau.api.especificaciones.OperacionSpecs;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.OperacionMapper;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OperacionServicio {

    private final OperacionRepositorio operaciones;
    private final EmbarcacionRepositorio embarcaciones;
    private final PersonalRepositorio personal;
    private final OperacionMapper mapper;
    private final HorarioOperacion horario;

    public OperacionServicio(OperacionRepositorio operaciones, EmbarcacionRepositorio embarcaciones,
                             PersonalRepositorio personal, OperacionMapper mapper, HorarioOperacion horario) {
        this.operaciones = operaciones;
        this.embarcaciones = embarcaciones;
        this.personal = personal;
        this.mapper = mapper;
        this.horario = horario;
    }

    // ---------------------------------------------------------------- Consultas

    public OperacionResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    public Page<OperacionResumen> listar(OperacionFilter filter, Pageable pageable) {
        return operaciones.findAll(OperacionSpecs.de(filter), pageable).map(mapper::toResumen);
    }

    /** Para el taquillero: operaciones en curso en las que se puede abordar en su embarcadero. */
    public List<OperacionResumen> abiertasEn(Embarcaderos embarcadero) {
        return operaciones.findByEstadoOrderByFechaHoraInicio(EstadoOperacion.EN_CURSO).stream()
                .filter(o -> embarcadero == null || o.admiteAbordajeEn(embarcadero))
                .map(mapper::toResumen)
                .toList();
    }

    // ---------------------------------------------------------------- Creación (zarpe)

    @Transactional
    public OperacionResponse crear(CrearOperacionRequest r, UUID personalId) {
        Personal registrador = buscarPersonal(personalId);
        exigirRol(registrador, Rol.OPERADOR_EMBARCACION, Rol.SUPERVISOR);
        if (!registrador.isContratoActivo()) {
            throw new BusinessException("Su contrato no se encuentra activo: no puede registrar operaciones");
        }

        Embarcacion embarcacion = embarcaciones.findById(r.embarcacionId())
                .orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));
        if (operaciones.existsByEmbarcacionIdAndEstado(embarcacion.getId(), EstadoOperacion.EN_CURSO)) {
            throw new BusinessException("La embarcación " + embarcacion.getNombre()
                    + " ya tiene una operación en curso: finalícela o suspéndala antes de abrir otra");
        }

        Instant inicio = ahoraSiNulo(r.fechaHoraInicio());
        horario.exigirZarpePermitido(inicio);

        Operacion op = new Operacion(embarcacion, r.embarcaderoOrigen(), r.embarcaderoDestino(),
                inicio, r.estadoNave(), mapper.toVerificacion(r.verificacion()),
                r.justificacionVerificacion(), r.condicionClimatica(), r.nivelRio(),
                NovedadMecanicaDTO.toDominio(r.condicionesMecanicas()), registrador);

        Set<UUID> ids = new LinkedHashSet<>(r.tripulacionIds());
        if (registrador.getRol() == Rol.OPERADOR_EMBARCACION) {
            ids.add(registrador.getId());                    // quien zarpa va a bordo
        }
        for (UUID id : ids) {
            Personal p = buscarPersonal(id);
            validarTripulante(p);
            op.agregarTripulante(p);
        }

        // Respaldo en BD por si la bandera de disponibilidad quedó desincronizada
        Set<UUID> tripulantes = op.getTripulacion().stream().map(Personal::getId).collect(Collectors.toSet());
        if (operaciones.existsByEstadoAndTripulacion_IdIn(EstadoOperacion.EN_CURSO, tripulantes)) {
            throw new BusinessException("Uno o más tripulantes ya están en otra operación en curso");
        }
        if (op.cantidadDeOperadores() < 2) {
            throw new BusinessException("Deben haber 2 operadores de embarcación para iniciar la operación");
        }
        if (!op.contieneGuia()) {
            throw new BusinessException("La tripulación debe incluir un guía de turismo (§7.1.4)");
        }

        op.getTripulacion().forEach(p -> p.setDisponibilidad(false));
        return mapper.toResponse(operaciones.save(op));
    }

    // ---------------------------------------------------------------- Actualización y acciones

    @Transactional
    public OperacionResponse actualizar(UUID id, ActualizarOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        op.actualizarBitacora(r.condicionClimatica(), r.nivelRio(), r.novedades(), r.observaciones());
        if (r.condicionesMecanicas() != null) {
            op.registrarCondicionesMecanicas(NovedadMecanicaDTO.toDominio(r.condicionesMecanicas()));
        }
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse agregarTripulante(UUID id, UUID personalId, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        Personal p = buscarPersonal(personalId);
        validarTripulante(p);
        op.agregarTripulante(p);
        p.setDisponibilidad(false);
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse eliminarTripulante(UUID id, UUID personalId, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        Personal p = buscarPersonal(personalId);
        op.eliminarTripulante(p);
        p.devolverDisponibilidad();
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse registrarParada(UUID id, ParadaRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        horario.exigirDentroDeJornada(op.getFechaHoraInicio(), r.horaSalida(), "salida de la parada");
        op.registrarParada(r.embarcadero(), r.horaLlegada(), r.horaSalida());
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse finalizar(UUID id, FinalizarOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        Instant fin = ahoraSiNulo(r.fechaHoraFin());
        horario.exigirDentroDeJornada(op.getFechaHoraInicio(), fin, "llegada");
        op.finalizar(fin, r.horometroFinal());
        liberarTripulacion(op);
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse suspender(UUID id, SuspenderOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        Instant momento = ahoraSiNulo(r.momento());
        horario.exigirDentroDeJornada(op.getFechaHoraInicio(), momento, "suspensión");
        op.suspender(momento, r.motivo());
        liberarTripulacion(op);
        return mapper.toResponse(op);
    }

    // ---------------------------------------------------------------- Auxiliares

    private Operacion buscar(UUID id) {
        return operaciones.findById(id).orElseThrow(() -> new NotFoundException("Operación no encontrada"));
    }

    private Personal buscarPersonal(UUID id) {
        return personal.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado: " + id));
    }

    /** Modifica la bitácora el supervisor o un operador que vaya a bordo. */
    private Operacion buscarParaModificar(UUID operacionId, UUID usuarioId) {
        Operacion op = buscar(operacionId);
        Personal usuario = buscarPersonal(usuarioId);
        boolean autorizado = usuario.getRol() == Rol.SUPERVISOR
                || (usuario.getRol() == Rol.OPERADOR_EMBARCACION && op.esTripulante(usuario));
        if (!autorizado) {
            throw new BusinessException("Solo el supervisor o un operador de la tripulación puede modificar esta operación");
        }
        return op;
    }

    /** Toda la tripulación queda disponible, no solo quien cierra (el supervisor no va a bordo). */
    private static void liberarTripulacion(Operacion op) {
        op.getTripulacion().forEach(Personal::devolverDisponibilidad);
    }

    private static void validarTripulante(Personal p) {
        if (!p.isContratoActivo()) {
            throw new BusinessException("El tripulante " + p.nombreCompleto() + " no tiene contrato activo");
        }
        if (!p.isDisponibilidad()) {
            throw new BusinessException("El tripulante " + p.nombreCompleto() + " se encuentra en otra operación");
        }
    }

    private static void exigirRol(Personal p, Rol... permitidos) {
        for (Rol r : permitidos) {
            if (p.getRol() == r) {
                return;
            }
        }
        throw new BusinessException("El rol " + p.getRol() + " no puede realizar esta acción");
    }

    private static Instant ahoraSiNulo(Instant valor) {
        return valor != null ? valor : Instant.now();
    }
}