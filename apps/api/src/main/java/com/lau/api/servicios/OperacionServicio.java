package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.EstadoOperacion;
import com.lau.api.dominio.enums.Rol;
import com.lau.api.dto.operacion.*;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.OperacionMapper;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.OperacionSpecs;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public OperacionServicio(OperacionRepositorio operaciones, EmbarcacionRepositorio embarcaciones,
                             PersonalRepositorio personal, OperacionMapper mapper) {
        this.operaciones = operaciones;
        this.embarcaciones = embarcaciones;
        this.personal = personal;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- Consultas

    public OperacionResponse obtener(UUID id) {
        return mapper.toResponse(buscar(id));
    }

    public Page<OperacionResumen> listar(LocalDate fecha, EstadoOperacion estado, Embarcaciones embarcacion,
                                         Pageable pageable) {
        Specification<Operacion> filtro = Specification.allOf(
                OperacionSpecs.dia(fecha, COLOMBIA),
                OperacionSpecs.estado(estado),
                OperacionSpecs.embarcacion(embarcacion));
        return operaciones.findAll(filtro, pageable).map(mapper::toResumen);
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
    public OperacionResponse crear(CrearOperacionRequest r, UUID persoanlId) {
        Personal registrador = buscarPersonal(persoanlId);
        exigirRol(registrador, Rol.OPERADOR_EMBARCACION, Rol.SUPERVISOR);

        Embarcacion embarcacion = embarcaciones.findById(r.embarcacionId())
                .orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));
        if (operaciones.existsByEmbarcacionIdAndEstado(embarcacion.getId(), EstadoOperacion.EN_CURSO)) {
            throw new BusinessException("La embarcación " + embarcacion.getNombre()
                    + " ya tiene una operación en curso: finalícela o suspéndala antes de abrir otra");
        }
        System.out.println(r);
        Operacion op = new Operacion(embarcacion, r.embarcaderoOrigen(), r.embarcaderoDestino(),
                ahoraSiNulo(r.fechaHoraInicio()), r.estadoNave(), mapper.toVerificacion(r.verificacion()),
                r.justificacionVerificacion(), r.condicionClimatica(), r.nivelRio(), r.condicionesMecanicas(),
                r.detalleCondicionesMecanicas(), registrador);

        r.tripulacionIds().forEach(id -> op.agregarTripulante(buscarPersonal(id)));
        if (registrador.getRol() == Rol.OPERADOR_EMBARCACION) {
            op.agregarTripulante(registrador);          // quien zarpa va a bordo
        }
        exigirOperadorABordo(op);

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
    public OperacionResponse registrarParada(UUID id, ParadaRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);

        op.registrarParada(r.embarcadero(), r.horaLlegada(), r.horaSalida());
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse finalizar(UUID id, FinalizarOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
        op.finalizar(ahoraSiNulo(r.fechaHoraFin()), r.horometroFinal());
        return mapper.toResponse(op);
    }

    @Transactional
    public OperacionResponse suspender(UUID id, SuspenderOperacionRequest r, UUID usuarioId) {
        Operacion op = buscarParaModificar(id, usuarioId);
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

    private static void exigirOperadorABordo(Operacion op) {
        boolean hayOperador = op.getTripulacion().stream().anyMatch(p -> p.getRol() == Rol.OPERADOR_EMBARCACION);
        if (!hayOperador) {
            throw new BusinessException("La tripulación debe incluir al menos un operador de embarcación");
        }
    }

    private static Instant ahoraSiNulo(Instant valor) {
        return valor != null ? valor : Instant.now();
    }
}