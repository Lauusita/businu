package com.lau.api.dominio;

import com.lau.api.dominio.enums.seguimiento.*;
import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ayuda a la navegación del recorrido (§3, §4.4), clasificada según el
 * Manual de Ayudas a la Navegación Colombiana (DIMAR, Región B de la IALA).
 */
@Getter
@Entity
@Table(name = "ayudas_navegacion")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AyudaNavegacion extends BaseEntity {

    /** Código visible en campo, p. ej. "B-03" o "BZ-01". */
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    // ---- Clasificación (Manual DIMAR, cap. 2 y 4) ----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstructuraAyuda estructura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FuncionMarca funcion;

    // ---- Luz (solo si es luminosa) ----

    @Column(nullable = false)
    private boolean luminosa;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_luz", length = 20)
    private ColorLuz colorLuz;

    /** Característica de la luz, p. ej. "Fl. G 3 s" o "Q (3) 10 s". */
    @Column(name = "ritmo_luz", length = 40)
    private String ritmoLuz;

    /** Forma, colores, marca de tope, o qué es cuando la estructura es OTRA. */
    @Column(nullable = false, length = 300)
    private String descripcion;

    // ---- Ubicación ----

    @Enumerated(EnumType.STRING)
    @Column(name = "tramo_desde", nullable = false, length = 30)
    private Embarcaderos tramoDesde;

    @Enumerated(EnumType.STRING)
    @Column(name = "tramo_hasta", nullable = false, length = 30)
    private Embarcaderos tramoHasta;

    @Column(name = "referencia_ubicacion", nullable = false, length = 200)
    private String referenciaUbicacion;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(nullable = false)
    private boolean activa = true;

    public AyudaNavegacion(String codigo, EstructuraAyuda estructura, FuncionMarca funcion,
                           boolean luminosa, ColorLuz colorLuz, String ritmoLuz, String descripcion,
                           Embarcaderos tramoDesde, Embarcaderos tramoHasta, String referenciaUbicacion,
                           BigDecimal latitud, BigDecimal longitud) {
        if (tramoDesde == tramoHasta) {
            throw new BusinessException("El tramo debe estar entre dos embarcaderos distintos");
        }
        if (estructura == EstructuraAyuda.OTRA && (descripcion == null || descripcion.isBlank())) {
            throw new BusinessException("Describa la ayuda a la navegación");
        }
        if (estructura == EstructuraAyuda.LUZ_SECTOR && !luminosa) {
            throw new BusinessException("Una luz de sector es, por definición, luminosa");
        }
        if (luminosa) {
            if (colorLuz == null) {
                throw new BusinessException("Indique el color de la luz");
            }
            ColorLuz exigido = funcion.colorExigido();
            if (exigido != null && exigido != colorLuz) {
                throw new BusinessException("Según el Manual de Ayudas a la Navegación (Región B), una marca "
                        + funcion + " usa luz " + exigido + ", no " + colorLuz);
            }
        } else if (colorLuz != null || (ritmoLuz != null && !ritmoLuz.isBlank())) {
            throw new BusinessException("Una ayuda sin luz no tiene color ni ritmo de luz");
        }

        this.codigo = codigo.trim().toUpperCase();
        this.estructura = estructura;
        this.funcion = funcion;
        this.luminosa = luminosa;
        this.colorLuz = luminosa ? colorLuz : null;
        this.ritmoLuz = luminosa ? textoOPorDefecto(ritmoLuz, "No especificado") : null;
        this.descripcion = textoOPorDefecto(descripcion, "Sin descripción");
        this.tramoDesde = tramoDesde;
        this.tramoHasta = tramoHasta;
        this.referenciaUbicacion = referenciaUbicacion.trim();
        this.latitud = latitud;
        this.longitud = longitud;
    }

    /** Se retira del recorrido; su historial de registros se conserva. */
    public void desactivar() {
        this.activa = false;
    }

    private static String textoOPorDefecto(String valor, String porDefecto) {
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }
}