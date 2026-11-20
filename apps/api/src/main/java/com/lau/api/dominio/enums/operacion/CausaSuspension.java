package com.lau.api.dominio.enums.operacion;

import lombok.Getter;

/** Causas de suspensión, modificación o restricción de la operación (§7.1.7 del anexo). */
@Getter
public enum CausaSuspension {
    INSTRUCCION_AUTORIDAD("Instrucción o restricción de la autoridad competente"),
    CONDICION_CLIMATICA("Condiciones climáticas que afectan la navegación segura"),
    NIVEL_RIO("Variación significativa del nivel del río"),
    RIESGO_INUNDACION_CRECIENTE("Riesgo por inundación, creciente, avalancha u otro evento informado"),
    FALLA_TECNICA_MECANICA("Condición técnica, mecánica u operacional"),
    OTRA("Otra circunstancia que requiere medidas preventivas");

    private final String descripcion;

    CausaSuspension(String descripcion) {
        this.descripcion = descripcion;
    }
}
