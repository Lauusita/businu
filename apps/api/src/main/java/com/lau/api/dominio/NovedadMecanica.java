package com.lau.api.dominio;

import com.lau.api.dominio.enums.operacion.CondicionesMecanicas;
import com.lau.api.dominio.enums.seguimiento.Severidad;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Novedad mecánica u operacional: la reporta el piloto en la bitácora (§4.1) y el técnico en el
 * mantenimiento (§4.3). Mismo catálogo y misma forma en ambos, para poder cruzarlos en el informe.
 * Una lista vacía significa "sin novedad".
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NovedadMecanica {

    public static final String SIN_DETALLE = "Sin detalle";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CondicionesMecanicas condicion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severidad severidad;

    /** Obligatorio si la condición es OTRA. */
    @Column(nullable = false, columnDefinition = "text")
    private String detalle;

    public NovedadMecanica(CondicionesMecanicas condicion, Severidad severidad, String detalle) {
        if (condicion == CondicionesMecanicas.OTRA && (detalle == null || detalle.isBlank())) {
            throw new BusinessException("Describa la novedad mecánica marcada como OTRA");
        }
        this.condicion = condicion;
        this.severidad = severidad;
        this.detalle = (detalle == null || detalle.isBlank()) ? SIN_DETALLE : detalle.trim();
    }

    /** Cada condición se reporta una sola vez (excepto OTRA, que puede describir cosas distintas). */
    public static List<NovedadMecanica> validar(List<NovedadMecanica> lista) {
        if (lista == null) {
            return List.of();
        }
        Set<CondicionesMecanicas> vistas = EnumSet.noneOf(CondicionesMecanicas.class);
        for (NovedadMecanica n : lista) {
            if (n.condicion != CondicionesMecanicas.OTRA && !vistas.add(n.condicion)) {
                throw new BusinessException("La condición " + n.condicion + " está repetida");
            }
        }
        return lista;
    }
}