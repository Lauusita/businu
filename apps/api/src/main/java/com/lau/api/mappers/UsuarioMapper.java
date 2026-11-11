package com.lau.api.mappers;

import com.lau.api.dominio.Embarcacion;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.enums.GrupoEtario;
import com.lau.api.dominio.enums.Sexo;
import com.lau.api.dto.UsuarioRequest;
import com.lau.api.dto.UsuarioResponse;
import com.lau.api.dto.UsuarioResponse.EmbarcacionResumen;
import com.lau.api.dto.UsuarioResponse.EmbarcaderoResumen;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    /** Se llama dentro de la transacción: embarcación y registrador son relaciones LAZY. */
    public UsuarioResponse toResponse(Usuario u) {
        Embarcacion e = u.getEmbarcacion();
        return new UsuarioResponse(
                u.getId(),
                u.getFechaHora(),
                new EmbarcacionResumen(e.getId(), e.getNombre(), e.getPatente()),
                EmbarcaderoResumen.de(u.getEmbarcaderoOrigen()),
                EmbarcaderoResumen.de(u.getEmbarcaderoDestino()),
                u.getUsuariosTransportados(),
                enOrden(Sexo.class, u.getPorSexo()),
                enOrden(GrupoEtario.class, u.getPorGrupoEtario()),
                u.getRegistradoPor().getUsername(),
                u.getCreadoEn());
    }

    public Usuario requestToUsuario(UsuarioRequest u, Embarcacion e, Personal p) {
        return new Usuario(
                u.fechaHora(),
                e,
                u.embarcaderoOrigen(),
                u.embarcaderoDestino(),
                u.usuariosTransportados(),
                p
        );
    }

    /** EnumMap: el JSON sale en el orden del enum (PRIMERA_INFANCIA → PERSONA_MAYOR), no al azar. */
    private static <K extends Enum<K>> Map<K, Integer> enOrden(Class<K> tipo, Map<K, Integer> distribucion) {
        Map<K, Integer> ordenado = new EnumMap<>(tipo);
        ordenado.putAll(distribucion);
        return ordenado;
    }
}