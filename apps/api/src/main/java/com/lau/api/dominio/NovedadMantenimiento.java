package com.lau.api.dominio;

import com.lau.api.dominio.enums.operacion.CondicionesMecanicas;
import com.lau.api.dominio.enums.seguimiento.Severidad;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Novedad mecánica u operacional identificada en la intervención (§4.3). Reutiliza el mismo
 * catálogo de la bitácora (CondicionesMecanicas). Tabla mantenimiento_novedades.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NovedadMantenimiento {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CondicionesMecanicas condicion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severidad severidad;

    /** Obligatorio si la condición es OTRA; "Sin detalle" si no se escribe nada. */
    @Column(nullable = false, columnDefinition = "text")
    private String detalle;

    public NovedadMantenimiento(CondicionesMecanicas condicion, Severidad severidad, String detalle) {
        if (condicion == CondicionesMecanicas.SIN_NOVEDAD) {
            throw new BusinessException("Si no hubo novedades, envíe la lista de novedades vacía");
        }
        if (condicion == CondicionesMecanicas.OTRA && (detalle == null || detalle.isBlank())) {
            throw new BusinessException("Describa la novedad marcada como OTRA");
        }
        this.condicion = condicion;
        this.severidad = severidad;
        this.detalle = (detalle == null || detalle.isBlank()) ? "Sin detalle" : detalle.trim();
    }
}