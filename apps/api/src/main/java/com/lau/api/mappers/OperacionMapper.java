package com.lau.api.mappers;

import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.VerificacionPrevia;
import com.lau.api.dto.comun.NovedadMecanicaDTO;
import com.lau.api.dto.operacion.OperacionResponse;
import com.lau.api.dto.operacion.OperacionResponse.EmbarcacionResumen;
import com.lau.api.dto.operacion.OperacionResponse.ParadaResponse;
import com.lau.api.dto.operacion.OperacionResponse.Tripulante;
import com.lau.api.dto.operacion.OperacionResumen;
import com.lau.api.dto.operacion.VerificacionPreviaDTO;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class OperacionMapper {

    public VerificacionPrevia toVerificacion(VerificacionPreviaDTO d) {
        return new VerificacionPrevia(d.aceiteMotor(), d.combustible(), d.sistemaDireccion(), d.bombaAchique(),
                d.lucesNavegacion(), d.chalecosSalvavidas(), d.radioComunicacion());
    }

    /** Llamar dentro de la transacción: embarcación, tripulación, paradas y usuarios son LAZY. */
    public OperacionResponse toResponse(Operacion o) {
        VerificacionPrevia v = o.getVerificacion();
        return new OperacionResponse(
                o.getId(),
                new EmbarcacionResumen(o.getEmbarcacion().getId(), o.getEmbarcacion().getNombre(),
                        o.getEmbarcacion().getPatente()),
                o.getEmbarcaderoOrigen().getDescripcion(),
                o.getEmbarcaderoDestino().getDescripcion(),
                o.getFechaHoraInicio(),
                o.getFechaHoraFin(),
                o.getEstado(),
                o.getEstadoNave(),
                new VerificacionPreviaDTO(v.isAceiteMotor(), v.isCombustible(), v.isSistemaDireccion(),
                        v.isBombaAchique(), v.isLucesNavegacion(), v.isChalecosSalvavidas(), v.isRadioComunicacion()),
                o.getJustificacionVerificacion(),
                o.getCondicionClimatica(),
                o.getNivelRio(),
                o.getTripulacion().stream()
                        .sorted(Comparator.comparing(Personal::getRol))
                        .map(p -> new Tripulante(p.getId(), p.nombreCompleto(), p.getRol()))
                        .toList(),
                o.getParadas().stream()
                        .map(p -> new ParadaResponse(p.getEmbarcadero().getDescripcion(), p.getHoraLlegada(), p.getHoraSalida()))
                        .toList(),
                o.sinNovedadesMecanicas(),
                NovedadMecanicaDTO.desde(o.getCondicionesMecanicas()),
                o.getNovedades(),
                o.getObservaciones(),
                o.getMotivoSuspension(),
                o.getHorometroFinal(),
                o.totalUsuariosTransportados(),
                o.getRegistradoPor().nombreCompleto(),
                o.isCargueCombustible(),
                o.getMotivo() == null ? null : new OperacionResponse.SuspensionDTO(o.getMotivo().getCausa(),
                        o.getMotivo().getCausa().getDescripcion(), o.getMotivo().getDetalle(), o.getMotivo().getAutoridad()),
                o.getLecturasHorometro().stream()
                        .map(l -> new OperacionResponse.HorometroDTO(l.getMotor().getId(), l.getMotor().getSerial(),
                                l.getLecturaInicial(), l.getLecturaFinal(), l.horas()))
                        .toList(),
                o.horasMotor(),
                o.horasNavegacion(),
                o.getAbastecimientos().stream()
                        .map(a -> new OperacionResponse.CargueDTO(a.getId(), a.getFechaHora(), a.getGalones(), a.isTanqueLleno()))
                        .toList());
    }

    public OperacionResumen toResumen(Operacion o) {
        return new OperacionResumen(o.getId(), o.getEmbarcacion().getNombre(), o.getEmbarcaderoOrigen().getDescripcion(),
                o.getEmbarcaderoDestino().getDescripcion(), o.getFechaHoraInicio(), o.getFechaHoraFin(), o.getEstado(),
                o.totalUsuariosTransportados());
    }
}