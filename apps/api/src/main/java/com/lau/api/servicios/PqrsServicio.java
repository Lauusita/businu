package com.lau.api.servicios;

import com.lau.api.dominio.Personal;
import com.lau.api.dominio.Pqrs;
import com.lau.api.dominio.enums.pqrs.EstadoPqrs;
import com.lau.api.dominio.enums.pqrs.TipoPqrs;
import com.lau.api.dto.pqrs.*;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.DemasiadasSolicitudesException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.repositorio.PqrsRepositorio;
import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PQRS (§4.7). Se radican solo desde el formulario público; el supervisor gestiona. El límite de envíos
 * por dirección IP vive en memoria (basta para una instancia; no guarda la IP en la base).
 */
@Service
@Transactional(readOnly = true)
public class PqrsServicio {

    static final int MAX_POR_HORA = 5;
    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");

    private final PqrsRepositorio pqrs;
    private final PersonalRepositorio personal;
    private final Map<String, Deque<Instant>> envios = new ConcurrentHashMap<>();

    public PqrsServicio(PqrsRepositorio pqrs, PersonalRepositorio personal) {
        this.pqrs = pqrs;
        this.personal = personal;
    }

    @Transactional
    public RadicadoResponse radicar(RadicarPqrsRequest r, String ip) {
        if (r.sitioWeb() != null && !r.sitioWeb().isBlank()) {
            throw new BusinessException("Solicitud inválida");          // campo trampa: envío automático
        }
        exigirLimite(ip);
        String radicado = "PQRS-" + Year.now(COLOMBIA) + "-" + String.format("%06d", pqrs.siguienteConsecutivo());
        Pqrs p = pqrs.save(new Pqrs(radicado, r.tipo(), r.descripcion(), r.embarcadero(), r.fechaHecho(),
                r.nombre(), r.correo(), r.telefono(), Boolean.TRUE.equals(r.autorizaDatos())));
        return new RadicadoResponse(p.getRadicado(), p.getCreadoEn(),
                "Su solicitud quedó radicada con el número " + p.getRadicado() + ". Consérvelo para hacer seguimiento.");
    }

    public Page<PqrsResponse> listar(EstadoPqrs estado, TipoPqrs tipo, Pageable pageable) {
        Specification<Pqrs> filtro = Specification.allOf(
                (root, q, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado),
                (root, q, cb) -> tipo == null ? null : cb.equal(root.get("tipo"), tipo));
        return pqrs.findAll(filtro, pageable).map(PqrsServicio::toResponse);
    }

    public PqrsResponse obtener(UUID id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public PqrsResponse gestionar(UUID id, GestionarPqrsRequest r, UUID supervisorId) {
        Personal supervisor = personal.findById(supervisorId)
                .orElseThrow(() -> new NotFoundException("Personal no encontrado"));
        Pqrs p = buscar(id);
        p.gestionar(r.estado(), r.respuesta(), r.entidadTraslado(), r.observacionInterna(), supervisor);
        return toResponse(p);
    }

    // ---------------------------------------------------------------- Auxiliares

    /** Máximo MAX_POR_HORA radicaciones por IP en la última hora. */
    private void exigirLimite(String ip) {
        Instant ahora = Instant.now();
        Deque<Instant> lista = envios.computeIfAbsent(ip == null ? "desconocida" : ip, k -> new ArrayDeque<>());
        synchronized (lista) {
            while (!lista.isEmpty() && lista.peekFirst().isBefore(ahora.minus(Duration.ofHours(1)))) {
                lista.pollFirst();
            }
            if (lista.size() >= MAX_POR_HORA) {
                throw new DemasiadasSolicitudesException("Ha enviado demasiadas solicitudes. Intente más tarde.");
            }
            lista.addLast(ahora);
        }
    }

    private Pqrs buscar(UUID id) {
        return pqrs.findById(id).orElseThrow(() -> new NotFoundException("PQRS no encontrada"));
    }

    static PqrsResponse toResponse(Pqrs p) {
        return new PqrsResponse(p.getId(), p.getRadicado(), p.getCreadoEn(), p.getTipo(), p.getDescripcion(),
                p.getEmbarcadero(), p.getFechaHecho(), p.getNombre(), p.getCorreo(), p.getTelefono(),
                p.isAutorizaDatos(), p.getEstado(), p.getRespuesta(), p.getEntidadTraslado(),
                p.getObservacionInterna(), p.getFechaRespuesta(),
                p.getGestionadoPor() == null ? null : p.getGestionadoPor().nombreCompleto());
    }
}
