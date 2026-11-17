package com.lau.api.dominio;

import com.lau.api.dominio.enums.TipoCombustible;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Registro de combustible (§4.5). El abastecimiento se hace sin pasajeros a bordo (§7.1.10). */
@Getter
@Entity
@Table(name = "abastecimientos_combustible",
        indexes = @Index(name = "ix_abastecimiento_embarcacion_fecha", columnList = "embarcacion_id, fecha_hora"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AbastecimientoCombustible extends BaseEntity {

    public static final String SIN_OBSERVACIONES = "Sin observaciones";

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_combustible", nullable = false, length = 30)
    private TipoCombustible tipoCombustible;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal galones;

    /** Valor pagado según el recibo (COP). Alimenta el costo operacional del piloto. */
    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    /** Lectura del horómetro al abastecer: base para calcular el consumo por hora de motor. */
//    @Column(nullable = false, precision = 10, scale = 1)
//    private BigDecimal horometro;

    /** true si se llenó el tanque: solo entre dos tanques llenos se puede calcular el consumo. */
    @Column(name = "tanque_lleno", nullable = false)
    private boolean tanqueLleno;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soporte_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_abastecimiento_soporte"))
    private Adjunto soporte;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    @Column(nullable = false, columnDefinition = "text")
    private String observaciones = SIN_OBSERVACIONES;

    public AbastecimientoCombustible(Instant fechaHora, Embarcacion embarcacion, TipoCombustible tipo,
                                     BigDecimal galones, BigDecimal valorTotal, boolean tanqueLleno,
                                     Adjunto soporte, String observaciones, Personal registradoPor) {
        if (galones.signum() <= 0) {
            throw new BusinessException("La cantidad de galones debe ser mayor que cero");
        }
        if (valorTotal.signum() < 0) {
            throw new BusinessException("El valor total no puede ser negativo");
        }
        if (!"application/pdf".equals(soporte.getContentType())) {
            throw new BusinessException("El soporte debe ser un PDF");
        }
        this.fechaHora = fechaHora;
        this.embarcacion = embarcacion;
        this.tipoCombustible = tipo;
        this.galones = galones;
        this.valorTotal = valorTotal;
        this.tanqueLleno = tanqueLleno;
        this.soporte = soporte;
        this.registradoPor = registradoPor;
        this.observaciones = (observaciones == null || observaciones.isBlank()) ? SIN_OBSERVACIONES : observaciones.trim();
    }

    /** Precio por galón calculado del recibo. */
    public BigDecimal precioPorGalon() {
        return valorTotal.divide(galones, 2, RoundingMode.HALF_UP);
    }
}