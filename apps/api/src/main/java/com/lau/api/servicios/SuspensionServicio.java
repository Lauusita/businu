package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.MotivoSuspension;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.SuspensionEmbarcacion;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.suspension.LevantarSuspensionRequest;
import com.lau.api.dto.suspension.SuspenderEmbarcacionRequest;
import com.lau.api.dto.suspension.SuspensionResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.ForbiddenException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.SuspensionEmbarcacionRepositorio;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suspensión de una embarcación que está en el muelle (§7.1.7, R48), p. ej. por orden de una autoridad
 * antes del zarpe. Cada suspensión es un registro nuevo; mientras haya una vigente, la embarcación no
 * zarpa. La suspensión de un viaje en curso es otra cosa: queda en la operación y es definitiva.
 */
@Service
@Transactional(readOnly = true)
public class SuspensionServicio {

    private final SuspensionEmbarcacionRepositorio suspensiones;
    private final EmbarcacionRepositorio embarcaciones;
    private final OperacionRepositorio operaciones;
    private final PersonalRepositorio personal;

    public SuspensionServicio(SuspensionEmbarcacionRepositorio suspensiones, EmbarcacionRepositorio embarcaciones,
                              OperacionRepositorio operaciones, PersonalRepositorio personal) {
        this.suspensiones = suspensiones;
        this.embarcaciones = embarcaciones;
        this.operaciones = operaciones;
        this.personal = personal;
    }

    /** Embarcación en el muelle (sin viaje en curso). Para un viaje en curso: POST /operaciones/{id}/suspender. */
    @Transactional
    public SuspensionResponse suspender(UUID embarcacionId, SuspenderEmbarcacionRequest r, UUID usuarioId) {
        Personal quien = buscarPersonal(usuarioId);
        exigirOperadorOSupervisor(quien);
        Embarcacion embarcacion = buscarEmbarcacion(embarcacionId);
        if (operaciones.existsByEmbarcacionIdAndEstado(embarcacionId, EstadoOperacion.EN_CURSO)) {
            throw new BusinessException("La embarcación tiene un viaje en curso: suspéndalo con "
                    + "POST /operaciones/{id}/suspender para que quede en la bitácora");
        }
        exigirSinSuspensionVigente(embarcacionId);
        MotivoSuspension motivo = new MotivoSuspension(r.causa(), r.detalle(), r.autoridad());
        Instant inicio = r.fechaInicio() != null ? r.fechaInicio() : Instant.now();
        return toResponse(suspensiones.save(new SuspensionEmbarcacion(embarcacion, motivo, inicio, quien)));
    }

    @Transactional
    public SuspensionResponse levantar(UUID embarcacionId, UUID suspensionId, LevantarSuspensionRequest r,
                                       UUID usuarioId) {
        Personal quien = buscarPersonal(usuarioId);
        exigirOperadorOSupervisor(quien);
        SuspensionEmbarcacion s = suspensiones.findById(suspensionId)
                .filter(x -> x.getEmbarcacion().getId().equals(embarcacionId))
                .orElseThrow(() -> new NotFoundException("Suspensión no encontrada para esta embarcación"));
        s.levantar(r.fecha() != null ? r.fecha() : Instant.now(), r.observacion(), quien);
        return toResponse(s);
    }

    public List<SuspensionResponse> historial(UUID embarcacionId) {
        buscarEmbarcacion(embarcacionId);
        return suspensiones.findByEmbarcacionIdOrderByFechaInicioDesc(embarcacionId).stream()
                .map(this::toResponse).toList();
    }

    /** Lo usa OperacionServicio.crear: no se zarpa con una suspensión vigente. */
    public void exigirSinSuspensionVigente(UUID embarcacionId) {
        suspensiones.findFirstByEmbarcacionIdAndFechaFinIsNull(embarcacionId).ifPresent(s -> {
            throw new BusinessException("La embarcación " + s.getEmbarcacion().getNombre() + " está suspendida desde "
                    + s.getFechaInicio() + " (" + s.getMotivo().getCausa() + "). Levante la suspensión antes de continuar");
        });
    }

    // ---------------------------------------------------------------- Auxiliares

    private SuspensionResponse toResponse(SuspensionEmbarcacion s) {
        MotivoSuspension m = s.getMotivo();
        return new SuspensionResponse(
                s.getId(),
                s.getEmbarcacion().getId(),
                s.getEmbarcacion().getNombre(),
                m.getCausa(),
                m.getCausa().getDescripcion(),
                m.getDetalle(),
                m.getAutoridad(),
                s.getFechaInicio(),
                s.getFechaFin(),
                s.vigente(),
                s.getObservacionLevantamiento(),
                s.getRegistradoPor().nombreCompleto(),
                s.getLevantadaPor() == null ? null : s.getLevantadaPor().nombreCompleto());
    }

    private static void exigirOperadorOSupervisor(Personal p) {
        if (p.getRol() != Rol.SUPERVISOR && p.getRol() != Rol.OPERADOR_EMBARCACION) {
            throw new ForbiddenException("Solo supervisores y operadores de embarcación gestionan suspensiones");
        }
    }

    private Embarcacion buscarEmbarcacion(UUID id) {
        return embarcaciones.findById(id).orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));
    }

    private Personal buscarPersonal(UUID id) {
        return personal.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }
}
