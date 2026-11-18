package com.lau.api.dominio.enums.mantenimiento;

/**
 * Repuestos, insumos o elementos utilizados (§4.3). Orientado al servicio de motores Suzuki DF200;
 * validar contra el manual del fabricante. OTRO exige descripción.
 */
public enum RepuestoInsumo {
    // Motor: lubricación
    ACEITE_MOTOR,
    FILTRO_ACEITE,
    ACEITE_COLA,
    GRASA_MARINA,
    EMPAQUES_DRENAJE,
    // Motor: combustible
    FILTRO_COMBUSTIBLE,
    FILTRO_SEPARADOR_AGUA,
    MANGUERAS_COMBUSTIBLE,
    // Motor: encendido y refrigeración
    BUJIAS,
    IMPULSOR_BOMBA_AGUA,
    TERMOSTATO,
    EMPAQUES_SELLOS,
    // Propulsión
    HELICE,
    PASADOR_TUERCA_HELICE,
    ANODOS_SACRIFICIO,
    // Eléctrico
    BATERIA,
    FUSIBLES,
    CABLEADO_TERMINALES,
    BOMBILLOS_NAVEGACION,
    // Casco y cubierta
    BOMBA_ACHIQUE,
    DEFENSAS,
    CABOS_AMARRE,
    PINTURA_SELLANTE,
    ASIENTOS_HERRAJES,
    // Aire acondicionado
    FILTRO_AIRE_ACONDICIONADO,
    GAS_REFRIGERANTE,
    // Seguridad
    CHALECOS_SALVAVIDAS,
    AROS_SALVAVIDAS,
    EXTINTOR,
    BOTIQUIN,
    // Consumibles
    PRODUCTOS_LIMPIEZA,
    OTRO
}