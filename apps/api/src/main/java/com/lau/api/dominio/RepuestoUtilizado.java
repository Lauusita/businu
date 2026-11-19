package com.lau.api.dominio;

import com.lau.api.dominio.enums.mantenimiento.RepuestoInsumo;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Repuesto o insumo usado en una intervención. Tabla mantenimiento_repuestos. */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepuestoUtilizado {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RepuestoInsumo item;

    /** Obligatoria si el ítem es OTRO; "No aplica" en los demás. */
    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    /** Unidad: litros, galones, unidades, kit... */
    @Column(nullable = false, length = 20)
    private String unidad;

    /** Costo total del ítem en COP. Alimenta el costo operacional del piloto. */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal costo;

    public RepuestoUtilizado(RepuestoInsumo item, String descripcion, BigDecimal cantidad, String unidad,
                             BigDecimal costo) {
        if (item == RepuestoInsumo.OTRO && (descripcion == null || descripcion.isBlank())) {
            throw new BusinessException("Describa el repuesto o insumo marcado como OTRO");
        }
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new BusinessException("La cantidad de " + item + " debe ser mayor que cero");
        }
        if (costo != null && costo.signum() < 0) {
            throw new BusinessException("El costo de " + item + " no puede ser negativo");
        }
        this.item = item;
        this.descripcion = item == RepuestoInsumo.OTRO ? descripcion.trim() : "No aplica";
        this.cantidad = cantidad;
        this.unidad = (unidad == null || unidad.isBlank()) ? "unidades" : unidad.trim();
        this.costo = costo == null ? BigDecimal.ZERO : costo;
    }
}