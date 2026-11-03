package com.lau.api.domain;

import com.lau.api.domain.enums.Severidad;
import com.lau.api.exceptions.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Novedad mecánica u operacional identificada: ítem del catálogo o "Otra" con texto libre. */
@Getter
@Entity
@Table(name = "mantenimiento_novedad")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MantenimientoNovedad extends EntidadHija {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mantenimiento_id", nullable = false)
    private Mantenimiento mantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private CatalogoItem item;

    @Column(name = "otro_descripcion", length = 200)
    private String otroDescripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severidad severidad;

    @Column(columnDefinition = "text")
    private String detalle;

    MantenimientoNovedad(Mantenimiento mantenimiento, CatalogoItem item, String otroDescripcion,
                         Severidad severidad, String detalle) {
        if ((item == null) == (otroDescripcion == null || otroDescripcion.isBlank())) {
            throw new ReglaNegocioException("Indique una novedad del catálogo o la descripción de 'Otra' (solo una)");
        }
        this.mantenimiento = mantenimiento;
        this.item = item;
        this.otroDescripcion = otroDescripcion;
        this.severidad = severidad;
        this.detalle = detalle;
    }
}
