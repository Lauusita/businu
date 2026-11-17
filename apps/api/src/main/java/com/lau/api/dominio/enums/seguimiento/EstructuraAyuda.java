package com.lau.api.dominio.enums.seguimiento;

/** Tipo de estructura (Manual DIMAR, cap. 2 "Otras marcas" y cap. 4 puentes). */
public enum EstructuraAyuda {
    BOYA,                 // flotante, fondeada
    BALIZA,               // fija sobre pilote o estructura; puede tener o no luz
    FARO,
    LUZ_SECTOR,           // solo luz, distintos colores por sector
    ENFILACION,           // dos o más marcas alineadas (línea/rango principal)
    SENALIZACION_PUENTE,  // paneles o luces en pilares de puente (cap. 4)
    MARCA_AUXILIAR,       // marcas locales: muelles, embarcaderos, señales de tráfico
    OTRA
}