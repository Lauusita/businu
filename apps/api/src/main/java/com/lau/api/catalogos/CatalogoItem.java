package com.lau.api.catalogos;

import com.lau.api.compartido.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ítem de las listas de chequeo (repuestos/insumos y novedades). Los ítems no se borran,
 * se desactivan, para que los registros históricos sigan apuntando a ellos.
 */
@Getter
@Setter
@Entity
@Table(name = "catalogo_item", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "nombre"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogoItem extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCatalogo tipo;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean activo;

    public CatalogoItem(TipoCatalogo tipo, String categoria, String nombre, int orden) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.nombre = nombre;
        this.orden = orden;
        this.activo = true;
    }
}
