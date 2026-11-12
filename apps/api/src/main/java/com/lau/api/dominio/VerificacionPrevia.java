package com.lau.api.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Verificación previa de fluidos y condiciones básicas (§4.1). Se guarda en columnas de operaciones. */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionPrevia {

    @Column(name = "verif_aceite_motor", nullable = false)
    private boolean aceiteMotor;

    @Column(name = "verif_combustible", nullable = false)
    private boolean combustible;

    @Column(name = "verif_sistema_direccion", nullable = false)
    private boolean sistemaDireccion;

    @Column(name = "verif_bomba_achique", nullable = false)
    private boolean bombaAchique;

    @Column(name = "verif_luces_navegacion", nullable = false)
    private boolean lucesNavegacion;

    @Column(name = "verif_chalecos_salvavidas", nullable = false)
    private boolean chalecosSalvavidas;

    @Column(name = "verif_radio_comunicacion", nullable = false)
    private boolean radioComunicacion;

    public boolean todoConforme() {
        return aceiteMotor && combustible && sistemaDireccion && bombaAchique
                && lucesNavegacion && chalecosSalvavidas && radioComunicacion;
    }
}