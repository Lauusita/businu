package com.lau.api.servicios;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Mantenimiento;
import com.lau.api.dominio.enums.mantenimiento.TipoMantenimiento;
import com.lau.api.dto.embarcacion.EmbarcacionResponse;
import com.lau.api.dto.embarcacion.HojaDeVidaResponse;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.mappers.EmbarcacionMapper;
import com.lau.api.mappers.MantenimientoMapper;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import com.lau.api.repositorio.MantenimientoRepositorio;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas sobre las embarcaciones, incluida su hoja de vida (§4.3). */
@Service
@Transactional(readOnly = true)
public class EmbarcacionServicio {

    private final EmbarcacionRepositorio embarcaciones;
    private final MantenimientoRepositorio mantenimientos;
    private final EmbarcacionMapper mapper;
    private final MantenimientoMapper mantenimientoMapper;

    public EmbarcacionServicio(EmbarcacionRepositorio embarcaciones, MantenimientoRepositorio mantenimientos,
                               EmbarcacionMapper mapper, MantenimientoMapper mantenimientoMapper) {
        this.embarcaciones = embarcaciones;
        this.mantenimientos = mantenimientos;
        this.mapper = mapper;
        this.mantenimientoMapper = mantenimientoMapper;
    }

    /** Para otros servicios (mantenimiento, operación, combustible). */
    public Embarcacion buscar(UUID id) {
        return embarcaciones.findById(id).orElseThrow(() -> new NotFoundException("Embarcación no encontrada"));
    }

    public List<EmbarcacionResponse> listar() {
        return embarcaciones.findAll().stream().map(mapper::toResponse).toList();
    }

    /** Hoja de vida: la embarcación con sus motores y todas sus intervenciones. Se calcula, no se guarda. */
    public HojaDeVidaResponse hojaDeVida(UUID embarcacionId) {
        Embarcacion embarcacion = buscar(embarcacionId);
        List<Mantenimiento> lista = mantenimientos.findByEmbarcacionIdOrderByFechaIntervencionDesc(embarcacionId);
        Instant ahora = Instant.now();

        Instant ultima = lista.isEmpty() ? null : lista.get(0).getFechaIntervencion();
        Instant proxima = lista.stream()
                .map(Mantenimiento::getProximaIntervencion)
                .filter(Objects::nonNull)
                .filter(f -> f.isAfter(ahora))
                .min(Comparator.naturalOrder())
                .orElse(null);
        BigDecimal costoTotal = lista.stream().map(Mantenimiento::costoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new HojaDeVidaResponse(
                mapper.toResponse(embarcacion),
                lista.size(),
                lista.stream().filter(m -> m.getTipo() == TipoMantenimiento.PREVENTIVO).count(),
                lista.stream().filter(m -> m.getTipo() == TipoMantenimiento.CORRECTIVO).count(),
                ultima,
                proxima,
                costoTotal,
                lista.stream().map(mantenimientoMapper::toResponse).toList());
    }
}