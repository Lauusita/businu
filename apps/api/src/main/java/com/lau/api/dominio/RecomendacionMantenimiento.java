package com.lau.api.dominio;

import com.lau.api.dominio.enums.mantenimiento.TipoRecomendacion;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Alerta o recomendación con su justificación obligatoria (§4.3). Tabla mantenimiento_recomendaciones. */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecomendacionMantenimiento {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecomendacion tipo;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    public RecomendacionMantenimiento(TipoRecomendacion tipo, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new BusinessException("La recomendación " + tipo + " requiere una descripción");
        }
        this.tipo = tipo;
        this.descripcion = descripcion.trim();
    }
}