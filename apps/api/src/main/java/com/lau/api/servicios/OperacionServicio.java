package com.lau.api.servicios;

import com.lau.api.config.HorarioOperacion;
import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.EstadoOperacion;
import com.lau.api.dominio.enums.Rol;
import com.lau.api.dto.operacion.*;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.OperacionMapper;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.especificaciones.OperacionSpecs;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OperacionServicio {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");

    private final OperacionRepositorio operaciones;
    private final EmbarcacionRepositorio embarcaciones;
    private final PersonalRepositorio personal;
    private final OperacionMapper mapper;

    private final HorarioOperacion horarioOperacion;

    public OperacionServicio(OperacionRepositorio operaciones, EmbarcacionRepositorio embarcaciones,
                             PersonalRepositorio personal, OperacionMapper mapper, HorarioOperacion horarioOperacion) {
        this.operaciones = operaciones;
        this.embarcaciones = embarcaciones;
        this.personal = personal;
        this.mapper = mapper;
        this.horarioOperacion = horarioOperacion;
    }

    // ---------------------------------------------------------------- Consultas

    public OperacionResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    public Page<OperacionResumen> listar(OperacionFilter filter,
                                         Pageable pageable) {
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

        if (!registrador.isDisponibilidad()) throw new BusinessException("El registrador no se encuentra activo. Acceda con una cuenta que se encuentre activa.");

        if (operaciones.existsByEstadoAndTripulacion_IdIn(EstadoOperacion.EN_CURSO, Collections.singleton(personalId))) {
            throw new BusinessException("El tripulante ya se encuentra asignado a otra operación en curso");
        }

        Embarcacion embarcacion = embarcaciones.findById(r.embarcacionId())
                .orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));

        if (operaciones.existsByEmbarcacionIdAndEstado(embarcacion.getId(), EstadoOperacion.EN_CURSO)) {
            throw new BusinessException("La embarcación " + embarcacion.getNombre()
                    + " ya tiene una operación en curso: finalícela o suspéndala antes de abrir otra");
        }

        Instant inicio = ahoraSiNulo(r.fechaHoraInicio());
        horarioOperacion.exigirZarpePermitido(inicio);

        Operacion op = new Operacion(embarcacion, r.embarcaderoOrigen(), r.embarcaderoDestino(),
                inicio, r.estadoNave(), mapper.toVerificacion(r.verificacion()),
                r.justificacionVerificacion(), r.condicionClimatica(), r.nivelRio(), r.condicionesMecanicas(),
                r.detalleCondicionesMecanicas(), registrador);

        new LinkedHashSet<>(r.tripulacionIds()).forEach(id -> op.agregarTripulante(buscarPersonal(id)));

        if (registrador.getRol() == Rol.OPERADOR_EMBARCACION
                && op.getTripulacion().stream().noneMatch(t -> t.getId().equals(registrador.getId()))) {
            op.agregarTripulante(registrador);          // quien zarpa va a bordo
        }

        Set<UUID> ids = op.getTripulacion().stream().map(Personal::getId).collect(Collectors.toSet());
        if (operaciones.existsByEstadoAndTripulacion_IdIn(EstadoOperacion.EN_CURSO, ids)) {
            throw new BusinessException("Uno o más tripulantes ya están en otra operación en curso");
        }
        if (op.getTripulacion().stream().anyMatch(p -> !p.isContratoActivo())) {
            throw new BusinessException("La tripulación incluye personal sin contrato activo");
        }
        if (op.cantidadDeOperadores(op.getTripulacion()) < 2) {
            throw new BusinessException("Deben haber 2 conductores para iniciar la operación.");
        }
        if (!op.contieneGuia(op.getTripulacion())) {
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
            op.registrarCondicionesMecanicas(r.condicionesMecanicas(), r.detalleCondicionesMecanicas());
        }
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse agregarTripulante(UUID id, UUID personalId, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        op.agregarTripulante(buscarPersonal(personalId));
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse eliminarTripulante(UUID id, UUID personalId, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        op.eliminarTripulante(buscarPersonal(personalId));
        return mapper.toResponse(op);
    }
    @Transactional
    public OperacionResponse registrarParada(UUID id, ParadaRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);

        op.registrarParada(r.embarcadero(), r.horaLlegada(), r.horaSalida());
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse finalizar(UUID id, FinalizarOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        Personal p = buscarPersonal(usuarioId);
        p.devolverDisponibilidad();
        op.finalizar(ahoraSiNulo(r.fechaHoraFin()), r.horometroFinal());
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse suspender(UUID id, SuspenderOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        Personal p = buscarPersonal(usuarioId);
        p.devolverDisponibilidad();
        op.suspender(ahoraSiNulo(r.momento()), r.motivo());
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

    private void validarTripulante(Personal p) {
        String nombre = p.getNombres() + " " + p.getApellidos();
        if (!p.isContratoActivo()) {
            throw new BusinessException("El tripulante " + nombre + " no tiene contrato activo");
        }
        if (!p.isDisponibilidad()) {
            throw new BusinessException("El tripulante " + nombre + " se encuentra en otra operación");
        }
    }
}