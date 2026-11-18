package com.lau.api.dominio;

import com.lau.api.dominio.enums.operacion.CausaSuspension;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Motivo tipificado de una suspensión (§7.1.7, R48). Misma idea que NovedadMecanica: causa de un
 * catálogo + detalle, con validación en el constructor.
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MotivoSuspension {

    public static final String SIN_DETALLE = "Sin detalle";
    public static final String NO_APLICA = "No aplica";

    @Enumerated(EnumType.STRING)
    @Column(name = "causa", length = 40)
    private CausaSuspension causa;

    /** Obligatorio si la causa es OTRA. */
    @Column(name = "detalle", columnDefinition = "text")
    private String detalle;

    /** Autoridad que imparte la instrucción: obligatoria si la causa es INSTRUCCION_AUTORIDAD. */
    @Column(name = "autoridad", length = 150)
    private String autoridad;

    public MotivoSuspension(CausaSuspension causa, String detalle, String autoridad) {
        if (causa == null) {
            throw new BusinessException("Indique la causa de la suspensión");
        }
        if (causa == CausaSuspension.OTRA && vacio(detalle)) {
            throw new BusinessException("Describa la causa de la suspensión marcada como OTRA");
        }
        if (causa == CausaSuspension.INSTRUCCION_AUTORIDAD && vacio(autoridad)) {
            throw new BusinessException("Indique la autoridad que ordenó la suspensión");
        }
        this.causa = causa;
        this.detalle = vacio(detalle) ? SIN_DETALLE : detalle.trim();
        this.autoridad = causa == CausaSuspension.INSTRUCCION_AUTORIDAD ? autoridad.trim() : NO_APLICA;
    }

    /** Texto legible, p. ej. para el campo histórico motivoSuspension. */
    public String texto() {
        String base = causa.getDescripcion() + ": " + detalle;
        return causa == CausaSuspension.INSTRUCCION_AUTORIDAD ? base + " (" + autoridad + ")" : base;
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
