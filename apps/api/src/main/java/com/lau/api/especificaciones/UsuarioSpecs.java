package com.lau.api.especificaciones;

import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dto.usuario.UsuarioFilter;
import jakarta.persistence.criteria.MapJoin;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecs {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");

    private UsuarioSpecs() {
    }

    public static Specification<Usuario> de(UsuarioFilter f) {
        return Specification.allOf(
                embarcacion(f.embarcacion()),
                puertoAbordaje(f.puertoAbordaje()),
                puertoDestino(f.puertoDestino()),
                cantidadEntre(f.cantidadMinima(), f.cantidadMaxima()),
                dia(f.fecha()));
    }

    /** La embarcación está en la operación, no en el usuario. */
    public static Specification<Usuario> embarcacion(Embarcaciones nombre) {
        return (r, q, cb) -> nombre == null ? null
                : cb.equal(r.get("operacion").get("embarcacion").get("nombre"), nombre);
    }

    public static Specification<Usuario> puertoAbordaje(Embarcaderos e) {
        return (r, q, cb) -> e == null ? null : cb.equal(r.get("embarcaderoAbordaje"), e);
    }

    /** porDestino es un Map<Embarcaderos, Integer>: se filtra por la clave del mapa. */
    public static Specification<Usuario> puertoDestino(Embarcaderos destino) {
        return (r, q, cb) -> {
            if (destino == null) return null;
            MapJoin<Usuario, Embarcaderos, Integer> d = r.joinMap("porDestino");
            q.distinct(true);                       // evita filas repetidas por el join
            return cb.equal(d.key(), destino);
        };
    }

    public static Specification<Usuario> cantidadEntre(Integer min, Integer max) {
        return (r, q, cb) -> {
            if (min == null && max == null) return null;
            if (max == null) return cb.greaterThanOrEqualTo(r.get("usuariosTransportados"), min);
            if (min == null) return cb.lessThanOrEqualTo(r.get("usuariosTransportados"), max);
            return cb.between(r.get("usuariosTransportados"), min, max);
        };
    }

    /** Un día calendario en Colombia = [00:00, 00:00 del día siguiente). */
    public static Specification<Usuario> dia(LocalDate fecha) {
        return (r, q, cb) -> fecha == null ? null : cb.and(
                cb.greaterThanOrEqualTo(r.get("fechaHora"), fecha.atStartOfDay(COLOMBIA).toInstant()),
                cb.lessThan(r.get("fechaHora"), fecha.plusDays(1).atStartOfDay(COLOMBIA).toInstant()));
    }
}