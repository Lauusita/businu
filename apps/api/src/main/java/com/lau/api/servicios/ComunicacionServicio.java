package com.lau.api.servicios;

import com.lau.api.dominio.ComunicacionFalla;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.comunicacion.TipoReferenciaFalla;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.comunicacion.ComunicacionRequest;
import com.lau.api.dto.comunicacion.ComunicacionResponse;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.ForbiddenException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.ComunicacionFallaRepositorio;
import com.lau.api.repositorio.MantenimientoRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.SuspensionEmbarcacionRepositorio;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Constancia de comunicación de fallas por correo (§7.1.12, R50).
 *
 * <p>Sin transacción de método a propósito: la constancia se guarda PENDIENTE antes de intentar el
 * envío, y el resultado (ENVIADO con Message-ID, o FALLIDO con la causa) se guarda después. Así queda
 * constancia aunque el servidor de correo falle, y nunca se marca ENVIADO sin confirmación.
 */
@Service
public class ComunicacionServicio {

    private final ComunicacionFallaRepositorio comunicaciones;
    private final PersonalRepositorio personal;
    private final OperacionRepositorio operaciones;
    private final SuspensionEmbarcacionRepositorio suspensiones;
    private final MantenimientoRepositorio mantenimientos;
    private final CorreoServicio correo;
    private final String correoSupervisor;

    public ComunicacionServicio(ComunicacionFallaRepositorio comunicaciones, PersonalRepositorio personal,
                                OperacionRepositorio operaciones, SuspensionEmbarcacionRepositorio suspensiones,
                                MantenimientoRepositorio mantenimientos, CorreoServicio correo,
                                @Value("${businu.correo.supervisor:}") String correoSupervisor) {
        this.comunicaciones = comunicaciones;
        this.personal = personal;
        this.operaciones = operaciones;
        this.suspensiones = suspensiones;
        this.mantenimientos = mantenimientos;
        this.correo = correo;
        this.correoSupervisor = correoSupervisor;
    }

    public ComunicacionResponse registrar(ComunicacionRequest r, UUID usuarioId) {
        Personal quien = personal.findById(usuarioId).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
        if (quien.getRol() != Rol.SUPERVISOR && quien.getRol() != Rol.OPERADOR_EMBARCACION) {
            throw new ForbiddenException("Solo supervisores y operadores de embarcación comunican fallas");
        }
        exigirReferencia(r.referenciaTipo(), r.referenciaId());
        String destinatario = (r.destinatario() == null || r.destinatario().isBlank()) ? correoSupervisor : r.destinatario();
        if (destinatario == null || destinatario.isBlank()) {
            throw new BusinessException("Indique el destinatario o configure el correo del supervisor (CORREO_SUPERVISOR)");
        }
        ComunicacionFalla c = comunicaciones.save(new ComunicacionFalla(r.referenciaTipo(), r.referenciaId(),
                destinatario, r.asunto(), r.mensaje(), quien));
        return toResponse(enviar(c), quien);
    }

    /** Reintenta una comunicación que falló (p. ej. cuando se configura o se recupera el servidor SMTP). */
    public ComunicacionResponse reintentar(UUID id) {
        ComunicacionFalla c = comunicaciones.findById(id)
                .orElseThrow(() -> new NotFoundException("Comunicación no encontrada"));
        return toResponse(enviar(c), personal.findById(c.getRegistradoPor().getId()).orElseThrow());
    }

    public List<ComunicacionResponse> listar(TipoReferenciaFalla tipo, UUID referenciaId) {
        return comunicaciones.findByReferenciaTipoAndReferenciaIdOrderByCreadoEnDesc(tipo, referenciaId).stream()
                .map(c -> toResponse(c, c.getRegistradoPor()))
                .toList();
    }

    // ---------------------------------------------------------------- Auxiliares

    private ComunicacionFalla enviar(ComunicacionFalla c) {
        c.iniciarIntento();
        try {
            CorreoServicio.Envio envio = correo.enviar(c.getDestinatario(), c.getAsunto(), c.getMensaje());
            c.marcarEnviado(envio.fecha(), envio.messageId());
        } catch (CorreoServicio.CorreoException e) {
            c.marcarFallido(e.getMessage());
        }
        return comunicaciones.save(c);
    }

    private void exigirReferencia(TipoReferenciaFalla tipo, UUID id) {
        boolean existe = switch (tipo) {
            case OPERACION -> operaciones.existsById(id);
            case SUSPENSION -> suspensiones.existsById(id);
            case MANTENIMIENTO -> mantenimientos.existsById(id);
        };
        if (!existe) {
            throw new NotFoundException("No existe " + tipo + " con id " + id);
        }
    }

    private static ComunicacionResponse toResponse(ComunicacionFalla c, Personal registradoPor) {
        return new ComunicacionResponse(c.getId(), c.getReferenciaTipo(), c.getReferenciaId(), c.getDestinatario(),
                c.getAsunto(), c.getEstado(), c.getCreadoEn(), c.getFechaEnvio(), c.getMessageId(), c.getError(),
                c.getIntentos(), registradoPor.nombreCompleto());
    }
}
