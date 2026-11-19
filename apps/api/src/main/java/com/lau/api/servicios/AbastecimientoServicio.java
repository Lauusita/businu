package com.lau.api.servicios;

import com.lau.api.dominio.AbastecimientoCombustible;
import com.lau.api.dominio.Adjunto;
import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.operacion.EstadoOperacion;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.dto.combustible.AbastecimientoRequest;
import com.lau.api.dto.combustible.AbastecimientoResponse;
import com.lau.api.dto.combustible.AbastecimientoResponse.SoporteDTO;
import com.lau.api.dto.combustible.ConsumoPeriodoResponse;
import com.lau.api.dominio.Operacion;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.ForbiddenException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.AbastecimientoRepositorio;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.OperacionRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new ForbiddenException("Solo operadores de embarcación y supervisores registran combustible");
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

    /**
     * Consumo por periodos entre dos abastecimientos con tanque lleno (R37, §4.5 "cuando sea técnicamente
     * posible"). Lo que se carga para volver a llenar el tanque es lo consumido desde el llenado anterior;
     * se reparte entre los viajes cerrados del periodo. Más reciente primero.
     */
    public List<ConsumoPeriodoResponse> consumo(UUID embarcacionId) {
        if (!embarcaciones.existsById(embarcacionId)) {
            throw new NotFoundException("Embarcación no encontrada");
        }
        List<AbastecimientoCombustible> cargas = abastecimientos.findByEmbarcacionIdOrderByFechaHoraAsc(embarcacionId);
        List<ConsumoPeriodoResponse> periodos = new ArrayList<>();
        int anterior = -1;
        for (int i = 0; i < cargas.size(); i++) {
            if (!cargas.get(i).isTanqueLleno()) {
                continue;
            }
            if (anterior >= 0) {
                periodos.add(periodo(embarcacionId, cargas, anterior, i));
            }
            anterior = i;
        }
        return periodos.reversed();
    }

    private ConsumoPeriodoResponse periodo(UUID embarcacionId, List<AbastecimientoCombustible> cargas, int desde, int hasta) {
        AbastecimientoCombustible inicial = cargas.get(desde);
        AbastecimientoCombustible fin = cargas.get(hasta);
        // Cargues parciales intermedios + el llenado final
        BigDecimal galones = cargas.subList(desde + 1, hasta + 1).stream()
                .map(AbastecimientoCombustible::getGalones).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Operacion> viajes = operaciones.findByEmbarcacionIdAndEstadoInAndFechaHoraInicioGreaterThanAndFechaHoraInicioLessThan(
                embarcacionId, EnumSet.of(EstadoOperacion.FINALIZADA, EstadoOperacion.SUSPENDIDA),
                inicial.getFechaHora(), fin.getFechaHora());
        BigDecimal horasNavegacion = viajes.stream().map(Operacion::horasNavegacion).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean horometroCompleto = !viajes.isEmpty() && viajes.stream().allMatch(Operacion::horometroCompleto);
        BigDecimal horasMotor = horometroCompleto
                ? viajes.stream().map(Operacion::horasMotor).reduce(BigDecimal.ZERO, BigDecimal::add)
                : null;

        return new ConsumoPeriodoResponse(
                inicial.getId(), fin.getId(), inicial.getFechaHora(), fin.getFechaHora(),
                galones, viajes.size(), horasNavegacion, horasMotor, horometroCompleto,
                dividir(galones, viajes.isEmpty() ? null : BigDecimal.valueOf(viajes.size())),
                dividir(galones, horasNavegacion),
                dividir(galones, horasMotor));
    }

    /** null si no hay con qué dividir (sin viajes, sin horas o sin horómetro). */
    private static BigDecimal dividir(BigDecimal galones, BigDecimal entre) {
        return (entre == null || entre.signum() == 0) ? null : galones.divide(entre, 2, RoundingMode.HALF_UP);
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
                a.getObservaciones(),
                a.getOperacion() == null ? null : a.getOperacion().getId());
    }
}