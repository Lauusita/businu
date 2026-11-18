package com.lau.api.especificaciones;

import com.lau.api.dominio.RegistroInfraestructura;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.EstadoSeguimiento;
import com.lau.api.dominio.enums.seguimiento.TipoRegistroInfraestructura;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class RegistroInfraestructuraSpecs {

    private RegistroInfraestructuraSpecs() {
    }

    public static Specification<RegistroInfraestructura> estado(EstadoSeguimiento e) {
        return (r, q, cb)
                -> e == null ? null : cb.equal(r.get("estado"), e);
    }

    public static Specification<RegistroInfraestructura> embarcadero(Embarcaderos e) {
        return (r, q, cb)
                -> e == null ? null : cb.equal(r.get("embarcadero"), e);
    }

    public static Specification<RegistroInfraestructura> ayuda(UUID ayudaId) {
        return (r, q, cb)
                -> ayudaId == null ? null : cb.equal(r.get("ayuda").get("id"), ayudaId);
    }

    public static Specification<RegistroInfraestructura> tipo(TipoRegistroInfraestructura t) {
        return (r, q, cb)
                -> t == null ? null : cb.equal(r.get("tipo"), t);
    }

    public static Specification<RegistroInfraestructura> entre(Instant desde, Instant hasta) {
        return (r, q, cb) -> {
            if (desde == null && hasta == null) return null;
            if (desde == null) return cb.lessThan(r.get("fechaHora"), hasta);
            if (hasta == null) return cb.greaterThanOrEqualTo(r.get("fechaHora"), desde);
            return cb.and(cb.greaterThanOrEqualTo(r.get("fechaHora"), desde), cb.lessThan(r.get("fechaHora"), hasta));
        };
    }
}