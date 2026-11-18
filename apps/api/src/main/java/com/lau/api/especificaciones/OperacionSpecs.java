package com.lau.api.especificaciones;

import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.EstadoOperacion;
import java.time.LocalDate;
import java.time.ZoneId;

import com.lau.api.dto.operacion.OperacionFilter;
import com.lau.api.dto.usuario.UsuarioFilter;
import org.springframework.data.jpa.domain.Specification;

/** Filtros opcionales del listado: un parámetro null no filtra. */
public final class OperacionSpecs {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private OperacionSpecs() {
    }

    public static Specification<Operacion> de(OperacionFilter f) {
        return Specification.allOf(
                estado(f.estado()),
                embarcacion(f.embarcacion()),
                dia(f.fecha(), COLOMBIA),
                origen(f.origen()),
                destino(f.destino())
        );
    }


    public static Specification<Operacion> estado(EstadoOperacion estado) {
        return (root, q, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Operacion> embarcacion(Embarcaciones nombre) {
        return (root, q, cb) -> nombre == null ? null : cb.equal(root.get("embarcacion").get("nombre"), nombre);
    }

    /** Día calendario en hora de Colombia, aunque el servidor guarde en UTC. */
    public static Specification<Operacion> dia(LocalDate dia, ZoneId zona) {
        return (root, q, cb) -> dia == null ? null : cb.and(
                cb.greaterThanOrEqualTo(root.get("fechaHoraInicio"), dia.atStartOfDay(zona).toInstant()),
                cb.lessThan(root.get("fechaHoraInicio"), dia.plusDays(1).atStartOfDay(zona).toInstant()));
    }

    public static Specification<Operacion> origen(Embarcaderos origen) {
        return (root, q, cb) -> origen == null ? null : cb.equal(root.get("origen"), origen);
    }

    public static Specification<Operacion> destino(Embarcaderos destino) {
        return (root, q, cb) -> destino == null ? null : cb.equal(root.get("destino"), destino);
    }
}