package com.lau.api.dominio.enums.seguimiento;

public enum ComponenteInfraestructura {

    // ---- Embarcaderos (§3 del anexo: muelles, rampas y estaciones de entrada) ----
    MUELLE(Ambito.EMBARCADERO, false),
    RAMPA_ACCESO(Ambito.EMBARCADERO, false),
    ESTACION_ENTRADA(Ambito.EMBARCADERO, false),
    PLATAFORMA_EMBARQUE(Ambito.EMBARCADERO, false),
    BARANDAS(Ambito.EMBARCADERO, false),
    DEFENSAS_MUELLE(Ambito.EMBARCADERO, false),
    TAQUILLA(Ambito.EMBARCADERO, false),

    // ---- Ayudas a la navegación (Manual DIMAR: posición, "lámparas, lentes, amarres", pintura) ----
    CUERPO_ESTRUCTURA(Ambito.AYUDA, false),
    FONDEO_AMARRE(Ambito.AYUDA, false),
    POSICION(Ambito.AYUDA, false),          // desplazada de su sitio (crecientes, palizadas)
    MARCA_TOPE(Ambito.AYUDA, false),
    LAMPARA(Ambito.AYUDA, true),
    LENTE(Ambito.AYUDA, true),
    PANEL_SOLAR_BATERIA(Ambito.AYUDA, true),
    PINTURA_REFLECTIVO(Ambito.AYUDA, false),

    // ---- Ambos ----
    ILUMINACION(Ambito.AMBOS, false),
    SENALIZACION(Ambito.AMBOS, false),
    GENERAL(Ambito.AMBOS, false),
    OTRO(Ambito.AMBOS, false);

    public enum Ambito { EMBARCADERO, AYUDA, AMBOS }

    private final Ambito ambito;
    private final boolean requiereLuz;

    ComponenteInfraestructura(Ambito ambito, boolean requiereLuz) {
        this.ambito = ambito;
        this.requiereLuz = requiereLuz;
    }

    public boolean aplicaAEmbarcadero() {
        return ambito != Ambito.AYUDA;
    }

    public boolean aplicaAAyuda() {
        return ambito != Ambito.EMBARCADERO;
    }

    /** Lámpara, lente y panel solar solo existen en ayudas luminosas. */
    public boolean requiereLuz() {
        return requiereLuz;
    }
}