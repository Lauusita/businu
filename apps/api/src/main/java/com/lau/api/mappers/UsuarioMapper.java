package com.lau.api.mappers;

import com.lau.api.dominio.*;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.GrupoEtario;
import com.lau.api.dominio.enums.Sexo;
import com.lau.api.dto.usuario.UsuarioRequest;
import com.lau.api.dto.usuario.UsuarioResponse;
import com.lau.api.dto.usuario.UsuarioResponse.EmbarcacionResumen;
import com.lau.api.dto.usuario.UsuarioResponse.EmbarcaderoResumen;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import com.lau.api.exceptions.NotFoundException;
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

    public Usuario requestToUsuario(UsuarioRequest r, Operacion o, Personal p) {
        Instant fecha = null;
        if (o.getParadas().size() == 1) {
            fecha = o.getFechaHoraInicio();
        } else {
            fecha = o.getParadas().stream()
                    .filter(parada -> parada.getEmbarcadero()
                            .equals(r.embarcaderoAbordaje()))
                    .map(Parada::getHoraLlegada)
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("No se encontró el embarcadero"));
        };


        Usuario u = new Usuario(fecha, o, r.embarcaderoAbordaje(), r.usuariosTransportados(), p);
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