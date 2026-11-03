package com.lau.api.domain;

import com.lau.api.domain.enums.TipoRecomendacion;
import com.lau.api.exceptions.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Alerta o recomendación (correctiva, preventiva o predictiva) con su justificación obligatoria. */
@Getter
@Entity
@Table(name = "mantenimiento_recomendacion", uniqueConstraints = @UniqueConstraint(columnNames = {"mantenimiento_id", "tipo"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MantenimientoRecomendacion extends EntidadHija {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mantenimiento_id", nullable = false)
    private Mantenimiento mantenimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecomendacion tipo;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    MantenimientoRecomendacion(Mantenimiento mantenimiento, TipoRecomendacion tipo, String descripcion) {
        this.mantenimiento = mantenimiento;
        this.tipo = tipo;
        setDescripcion(descripcion);
    }

    void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException("La recomendación " + tipo + " requiere una descripción");
        }
        this.descripcion = descripcion;
    }
}
