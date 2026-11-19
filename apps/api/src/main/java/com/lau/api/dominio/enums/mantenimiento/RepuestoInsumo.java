package com.lau.api.dominio.enums.mantenimiento;

/**
 * Repuestos, insumos o elementos utilizados (§4.3). Orientado al servicio de motores Suzuki DF200;
 * validar contra el manual del fabricante. OTRO exige descripción.
 */

import lombok.Getter;

public enum RepuestoInsumo {
    // Motor: lubricación
    ACEITE_MOTOR("Aceite de motor"),
    FILTRO_ACEITE("Filtro de aceite"),
    ACEITE_COLA("Aceite de cola"),
    GRASA_MARINA("Grasa marina"),
    EMPAQUES_DRENAJE("Empaques de drenaje"),

    // Motor: combustible
    FILTRO_COMBUSTIBLE("Filtro de combustible"),
    FILTRO_SEPARADOR_AGUA("Filtro separador de agua"),
    MANGUERAS_COMBUSTIBLE("Mangueras de combustible"),

    // Motor: encendido y refrigeración
    BUJIAS("Bujías"),
    IMPULSOR_BOMBA_AGUA("Impulsor de bomba de agua"),
    TERMOSTATO("Termostato"),
    EMPAQUES_SELLOS("Empaques y sellos"),

    // Propulsión
    HELICE("Hélice"),
    PASADOR_TUERCA_HELICE("Pasador y tuerca de hélice"),
    ANODOS_SACRIFICIO("Ánodos de sacrificio"),

    // Eléctrico
    BATERIA("Batería"),
    FUSIBLES("Fusibles"),
    CABLEADO_TERMINALES("Cableado y terminales"),
    BOMBILLOS_NAVEGACION("Bombillos de navegación"),

    // Casco y cubierta
    BOMBA_ACHIQUE("Bomba de achique"),
    DEFENSAS("Defensas"),
    CABOS_AMARRE("Cabos de amarre"),
    PINTURA_SELLANTE("Pintura y sellante"),
    ASIENTOS_HERRAJES("Asientos y herrajes"),

    // Aire acondicionado
    FILTRO_AIRE_ACONDICIONADO("Filtro de aire acondicionado"),
    GAS_REFRIGERANTE("Gas refrigerante"),

    // Seguridad
    CHALECOS_SALVAVIDAS("Chalecos salvavidas"),
    AROS_SALVAVIDAS("Aros salvavidas"),
    EXTINTOR("Extintor"),
    BOTIQUIN("Botiquín"),

    // Consumibles
    PRODUCTOS_LIMPIEZA("Productos de limpieza"),
    OTRO("Otro");

    @Getter
    private final String descripcion;

    RepuestoInsumo(String descripcion) {
        this.descripcion = descripcion;
    }
}