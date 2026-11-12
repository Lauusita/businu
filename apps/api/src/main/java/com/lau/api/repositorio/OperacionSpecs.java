package com.lau.api.repositorio;

import com.lau.api.dominio.Operacion;
import com.lau.api.dominio.enums.Embarcaciones;
import com.lau.api.dominio.enums.EstadoOperacion;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.data.jpa.domain.Specification;

/** Filtros opcionales del listado: un parámetro null no filtra. */
public final class OperacionSpecs {

    private OperacionSpecs() {
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
}