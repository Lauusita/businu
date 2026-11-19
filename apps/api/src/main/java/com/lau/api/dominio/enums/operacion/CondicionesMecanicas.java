package com.lau.api.dominio.enums.operacion;

import lombok.Getter;

/** Condiciones mecánicas y operacionales identificadas durante el viaje (§4.1). */
public enum CondicionesMecanicas {
    RUIDO_ANORMAL_MOTOR("Ruido anormal del motor"),
    SOBRECALENTAMIENTO("Sobrecalentamiento"),
    VIBRACION("Vibración"),
    PERDIDA_POTENCIA("Pérdida de potencia"),
    FALLA_ARRANQUE("Falla de arranque"),
    FUGA_ACEITE("Fuga de aceite"),
    FUGA_COMBUSTIBLE("Fuga de combustible"),
    FALLA_DIRECCION("Falla de dirección"),
    GOLPE_HELICE("Golpe en la hélice"),
    FALLA_ELECTRICA("Falla eléctrica"),
    FALLA_LUCES_NAVEGACION("Falla en luces de navegación"),
    FALLA_BOMBA_ACHIQUE("Falla en bomba de achique"),
    FALLA_AIRE_ACONDICIONADO("Falla en aire acondicionado"),
    INGRESO_AGUA("Ingreso de agua"),
    OTRA("Otra");

    @Getter
    private final String descripcion;

    CondicionesMecanicas(String descripcion) {
        this.descripcion = descripcion;
    }
}