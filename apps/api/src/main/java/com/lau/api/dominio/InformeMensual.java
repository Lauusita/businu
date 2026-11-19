package com.lau.api.dominio;

import com.lau.api.dominio.enums.EstadoInforme;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Informe mensual de operación (§4.7). Mientras está en BORRADOR, el supervisor escribe las partes que
 * no salen de los datos (resumen, conclusiones, recomendaciones). Al cerrarlo, las cifras se congelan
 * (datos_congelados, con su huella SHA-256) y el texto ya no se puede editar.
 */
@Getter
@Entity
@Audited
@Table(name = "informes_mensuales")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InformeMensual extends BaseEntity {

    /** Primer día del mes del informe. */
    @Column(nullable = false, unique = true, updatable = false)
    private LocalDate periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoInforme estado;

    @Column(name = "resumen_actividades", nullable = false, columnDefinition = "text")
    private String resumenActividades = "";

    @Column(nullable = false, columnDefinition = "text")
    private String conclusiones = "";

    @Column(nullable = false, columnDefinition = "text")
    private String recomendaciones = "";

    /** Otros documentos o soportes que pida el supervisor del contrato (§4.7, último punto). */
    @Column(name = "otros_documentos", nullable = false, columnDefinition = "text")
    private String otrosDocumentos = "";

    /** Cifras congeladas al cerrar (JSON de InformeMensualDatos). */
    @Column(name = "datos_congelados", columnDefinition = "text")
    private String datosCongelados;

    @Column(name = "huella_datos", length = 64)
    private String huellaDatos;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cerrado_por_id")
    private Personal cerradoPor;

    @Column(name = "fecha_entrega")
    private Instant fechaEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregado_por_id")
    private Personal entregadoPor;

    public InformeMensual(YearMonth mes) {
        this.periodo = mes.atDay(1);
        this.estado = EstadoInforme.BORRADOR;
    }

    public YearMonth mes() {
        return YearMonth.from(periodo);
    }

    /** Solo en BORRADOR. Un campo null no se modifica. */
    public void redactar(String resumen, String conclusiones, String recomendaciones, String otros) {
        if (estado != EstadoInforme.BORRADOR) {
            throw new BusinessException("El informe de " + mes() + " ya está " + estado + " y no se puede editar");
        }
        if (resumen != null) this.resumenActividades = resumen.trim();
        if (conclusiones != null) this.conclusiones = conclusiones.trim();
        if (recomendaciones != null) this.recomendaciones = recomendaciones.trim();
        if (otros != null) this.otrosDocumentos = otros.trim();
    }

    public void cerrar(String datosJson, String huella, Instant fecha, Personal quien) {
        if (estado != EstadoInforme.BORRADOR) {
            throw new BusinessException("El informe de " + mes() + " ya está " + estado);
        }
        if (resumenActividades.isBlank() || conclusiones.isBlank() || recomendaciones.isBlank()) {
            throw new BusinessException("Antes de cerrar, redacte el resumen de actividades, las conclusiones y las recomendaciones");
        }
        this.datosCongelados = datosJson;
        this.huellaDatos = huella;
        this.fechaCierre = fecha;
        this.cerradoPor = quien;
        this.estado = EstadoInforme.CERRADO;
    }

    public void entregar(Instant fecha, Personal quien) {
        if (estado != EstadoInforme.CERRADO) {
            throw new BusinessException("Solo se entrega un informe CERRADO (hoy está " + estado + ")");
        }
        this.fechaEntrega = fecha;
        this.entregadoPor = quien;
        this.estado = EstadoInforme.ENTREGADO;
    }
}
