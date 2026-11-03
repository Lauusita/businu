package com.lau.api.mantenimiento;

import com.lau.api.catalogos.CatalogoItem;
import com.lau.api.compartido.EntidadHija;
import com.lau.api.compartido.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Repuesto o insumo usado: un ítem del catálogo o, si es "Otro", una descripción libre. */
@Getter
@Entity
@Table(name = "mantenimiento_repuesto")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MantenimientoRepuesto extends EntidadHija {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mantenimiento_id", nullable = false)
    private Mantenimiento mantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private CatalogoItem item;

    @Column(name = "otro_descripcion", length = 200)
    private String otroDescripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false, length = 20)
    private String unidad;

    /** Costo total del ítem; alimenta el costo operacional del piloto. */
    @Column(precision = 14, scale = 2)
    private BigDecimal costo;

    MantenimientoRepuesto(Mantenimiento mantenimiento, CatalogoItem item, String otroDescripcion,
                          BigDecimal cantidad, String unidad, BigDecimal costo) {
        if ((item == null) == (otroDescripcion == null || otroDescripcion.isBlank())) {
            throw new ReglaNegocioException("Indique un ítem del catálogo o la descripción de 'Otro' (solo uno)");
        }
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        this.mantenimiento = mantenimiento;
        this.item = item;
        this.otroDescripcion = otroDescripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.costo = costo;
    }
}
