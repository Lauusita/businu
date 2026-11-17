package com.lau.api.dominio.enums.seguimiento;

/**
 * Qué indica la marca (Manual DIMAR, cap. 2). Colombia usa la Región B de la IALA:
 * verde a babor, rojo a estribor. Incluye el color de luz que exige el manual.
 */
public enum FuncionMarca {
    LATERAL_BABOR(ColorLuz.VERDE),
    LATERAL_ESTRIBOR(ColorLuz.ROJA),
    BIFURCACION_PREFERIDO_BABOR(ColorLuz.ROJA),
    BIFURCACION_PREFERIDO_ESTRIBOR(ColorLuz.VERDE),
    CARDINAL_NORTE(ColorLuz.BLANCA),
    CARDINAL_ESTE(ColorLuz.BLANCA),
    CARDINAL_SUR(ColorLuz.BLANCA),
    CARDINAL_OESTE(ColorLuz.BLANCA),
    PELIGRO_AISLADO(ColorLuz.BLANCA),
    AGUAS_SEGURAS(ColorLuz.BLANCA),
    ESPECIAL(ColorLuz.AMARILLA),
    NUEVO_PELIGRO(ColorLuz.AZUL_AMARILLA),
    INFORMATIVA(null);          // faros, enfilaciones, auxiliares: el color lo define la autoridad

    private final ColorLuz colorExigido;

    FuncionMarca(ColorLuz colorExigido) {
        this.colorExigido = colorExigido;
    }

    /** null = el manual no fija un color para esta función. */
    public ColorLuz colorExigido() {
        return colorExigido;
    }
}