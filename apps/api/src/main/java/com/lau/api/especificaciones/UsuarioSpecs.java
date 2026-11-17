package com.lau.api.especificaciones;

import com.lau.api.dominio.Usuario;
import com.lau.api.dominio.enums.Embarcaderos;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class UsuarioSpecs {

    private UsuarioSpecs() {
    }

    public static Specification<Usuario> embarcacion(UUID embarcacionId) {
        return (r, q, cb)
                -> embarcacionId == null ? null : cb.equal(r.get("embarcacion").get("id"), embarcacionId);
    }

    public static Specification<Usuario> embarcadero(Embarcaderos e) {
        return (r, q, cb)
                -> e == null ? null : cb.equal(r.get("embarcaderoAbordaje"), e);
    }

    public static Specification<Usuario> entre(Instant desde, Instant hasta) {
        return (r, q, cb) -> {
            if (desde == null && hasta == null) return null;
            if (desde == null) return cb.lessThan(r.get("fechaHora"), hasta);
            if (hasta == null) return cb.greaterThanOrEqualTo(r.get("fechaHora"), desde);
            return cb.and(cb.greaterThanOrEqualTo(r.get("fechaHora"), desde), cb.lessThan(r.get("fechaHora"), hasta));
        };
    }
}