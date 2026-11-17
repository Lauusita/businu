package com.lau.api.servicios;

import com.lau.api.dominio.AbastecimientoCombustible;
import com.lau.api.dominio.Adjunto;
import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.EstadoOperacion;
import com.lau.api.dominio.enums.Rol;
import com.lau.api.dto.combustible.AbastecimientoRequest;
import com.lau.api.dto.combustible.AbastecimientoResponse;
import com.lau.api.dto.combustible.AbastecimientoResponse.SoporteDTO;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.AbastecimientoRepositorio;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Registro de combustible (§4.5). */
@Service
@Transactional(readOnly = true)
public class AbastecimientoServicio {

    private final AbastecimientoRepositorio abastecimientos;
    private final EmbarcacionRepositorio embarcaciones;
    private final PersonalRepositorio personal;
    private final OperacionRepositorio operaciones;
    private final AdjuntoServicio adjuntoServicio;

    public AbastecimientoServicio(AbastecimientoRepositorio abastecimientos, EmbarcacionRepositorio embarcaciones,
                                  PersonalRepositorio personal, OperacionRepositorio operaciones,
                                  AdjuntoServicio adjuntoServicio) {
        this.abastecimientos = abastecimientos;
        this.embarcaciones = embarcaciones;
        this.personal = personal;
        this.operaciones = operaciones;
        this.adjuntoServicio = adjuntoServicio;
    }

    @Transactional
    public AbastecimientoResponse registrar(AbastecimientoRequest r, UUID usuarioId) {
        // 1. Validaciones que no dependen del archivo (si fallan, no se sube nada a S3)
        Personal registrador = personal.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Personal no encontrado"));
        if (registrador.getRol() != Rol.OPERADOR_EMBARCACION && registrador.getRol() != Rol.SUPERVISOR) {
            throw new BusinessException("Solo operadores de embarcación y supervisores registran combustible");
        }
        Embarcacion embarcacion = embarcaciones.findById(r.embarcacionId())
                .orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));
        if (operaciones.existsByEmbarcacionIdAndEstado(embarcacion.getId(), EstadoOperacion.EN_CURSO)) {
            throw new BusinessException("No se puede abastecer con una operación en curso: "
                    + "el abastecimiento se hace sin pasajeros a bordo (§7.1.10)");
        }

        Adjunto soporte = adjuntoServicio.vincular(r.soporteId(), registrador);

        AbastecimientoCombustible a = abastecimientos.save(new AbastecimientoCombustible(
                r.fechaHora() != null ? r.fechaHora() : Instant.now(),
                embarcacion, r.tipoCombustible(), r.galones(), r.valorTotal(), r.tanqueLleno(), soporte, r.observaciones(), registrador));
        return toResponse(a);
    }

    public AbastecimientoResponse obtener(UUID id) {
        return toResponse(buscar(id));
    }

    public Page<AbastecimientoResponse> listarPorEmbarcacion(UUID embarcacionId, Pageable pageable) {
        Page<AbastecimientoCombustible> pagina = abastecimientos.findByEmbarcacionId(embarcacionId, pageable);
        return pagina.map(this::toResponse);
    }
    public String urlSoporte(UUID id) {
        return adjuntoServicio.urlDescarga(buscar(id).getSoporte().getId());
    }

    private AbastecimientoCombustible buscar(UUID id) {
        return abastecimientos.findById(id).orElseThrow(() -> new NotFoundException("Abastecimiento no encontrado"));
    }

    private AbastecimientoResponse toResponse(AbastecimientoCombustible a) {
        Adjunto s = a.getSoporte();
        return new AbastecimientoResponse(
                a.getId(), a.getFechaHora(), a.getEmbarcacion().getId(), a.getEmbarcacion().getNombre(),
                a.getTipoCombustible(), a.getGalones(), a.getValorTotal(), a.precioPorGalon(), a.isTanqueLleno(),
                new SoporteDTO(s.getId(), s.getNombreOriginal(), s.getTamanoBytes(), s.getSha256()),
                a.getObservaciones());
    }
}