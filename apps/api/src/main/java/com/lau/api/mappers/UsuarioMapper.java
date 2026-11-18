package com.lau.api.mappers;

import com.lau.api.dominio.*;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pasajeros.GrupoEtario;
import com.lau.api.dominio.enums.pasajeros.Sexo;
import com.lau.api.dto.usuario.UsuarioRequest;
import com.lau.api.dto.usuario.UsuarioResponse;
import com.lau.api.dto.usuario.UsuarioResponse.EmbarcacionResumen;
import com.lau.api.dto.usuario.UsuarioResponse.EmbarcaderoResumen;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import com.lau.api.exceptions.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    /** Se llama dentro de la transacción: embarcación y registrador son relaciones LAZY. */
    public UsuarioResponse toResponse(Usuario u) {
        Embarcacion e = u.getOperacion().getEmbarcacion();
        return new UsuarioResponse(
                u.getId(),
                u.getFechaHora(),
                new EmbarcacionResumen(e.getId(), e.getNombre(), e.getPatente()),
                EmbarcaderoResumen.de(u.getEmbarcaderoAbordaje()),
                enOrden(Embarcaderos.class, u.getPorDestino()),
                u.getUsuariosTransportados(),
                enOrden(Sexo.class, u.getPorSexo()),
                enOrden(GrupoEtario.class, u.getPorGrupoEtario()),
                u.getRegistradoPor().nombreCompleto(),
                u.getCreadoEn());
    }

    public Usuario requestToUsuario(
            UsuarioRequest r,
            Operacion o,
            Personal p
    ) {
        Instant fecha = r.fechaHora() != null
                ? r.fechaHora()
                : Instant.now();

        if (fecha.isBefore(o.getFechaHoraInicio())) {
            throw new BusinessException(
                    "La fecha del abordaje no puede ser anterior al inicio de la operación"
            );
        }

        if (o.getFechaHoraFin() != null &&
                fecha.isAfter(o.getFechaHoraFin())) {
            throw new BusinessException(
                    "La fecha del abordaje no puede ser posterior al fin de la operación"
            );
        }

        // N-6: se puede abordar en cualquier embarcadero del recorrido (menos el destino), aunque el
        // operador todavía no haya registrado la parada. La regla la valida el constructor de Usuario.
        if (!o.admiteAbordajeEn(r.embarcaderoAbordaje())) {
            throw new BusinessException("No se puede abordar en " + r.embarcaderoAbordaje().getDescripcion()
                    + " en esta operación");
        }

        // N-23: si la parada ya está registrada, el abordaje no puede ser anterior a la llegada a ella
        o.getParadas().stream()
                .filter(parada -> parada.getEmbarcadero() == r.embarcaderoAbordaje() && parada.getHoraLlegada() != null)
                .findFirst()
                .ifPresent(parada -> {
                    if (fecha.isBefore(parada.getHoraLlegada())) {
                        throw new BusinessException("La fecha del abordaje no puede ser anterior a la llegada a "
                                + r.embarcaderoAbordaje().getDescripcion());
                    }
                });

        Usuario u = new Usuario(
                fecha,
                o,
                r.embarcaderoAbordaje(),
                r.usuariosTransportados(),
                p
        );

        r.porSexo().forEach(u::registrarSexo);
        r.porGrupoEtario().forEach(u::registrarGrupoEtario);
        r.porDestino().forEach(u::registrarDestino);

        return u;
    }

    /** EnumMap: el JSON sale en el orden del enum (PRIMERA_INFANCIA → PERSONA_MAYOR), no al azar. */
    private static <K extends Enum<K>> Map<K, Integer> enOrden(Class<K> tipo, Map<K, Integer> distribucion) {
        Map<K, Integer> ordenado = new EnumMap<>(tipo);
        ordenado.putAll(distribucion);
        return ordenado;
    }
}