package com.lau.api.dominio;

import org.hibernate.envers.Audited;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Suspensión de una embarcación en el muelle (§7.1.7, R48). Cada suspensión es un registro nuevo: el
 * historial no se sobrescribe. Mientras esté vigente (sin fecha de levantamiento), la embarcación no
 * puede zarpar. Las suspensiones de viajes en curso quedan en la operación, no aquí.
 */
@Getter
@Entity
@Audited
@Table(name = "suspensiones_embarcacion",
        indexes = @Index(name = "ix_suspension_embarcacion_inicio", columnList = "embarcacion_id, fecha_inicio"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SuspensionEmbarcacion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "causa", column = @Column(name = "causa", nullable = false, length = 40)),
            @AttributeOverride(name = "detalle", column = @Column(name = "detalle", nullable = false, columnDefinition = "text")),
            @AttributeOverride(name = "autoridad", column = @Column(name = "autoridad", nullable = false, length = 150))
    })
    private MotivoSuspension motivo;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    /** Levantamiento de la suspensión; null = vigente. */
    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @Column(name = "observacion_levantamiento", columnDefinition = "text")
    private String observacionLevantamiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "levantada_por_id")
    private Personal levantadaPor;

    public SuspensionEmbarcacion(Embarcacion embarcacion, MotivoSuspension motivo, Instant fechaInicio,
                                 Personal registradoPor) {
        this.embarcacion = embarcacion;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.registradoPor = registradoPor;
    }

    public boolean vigente() {
        return fechaFin == null;
    }

    public void levantar(Instant fecha, String observacion, Personal quien) {
        if (!vigente()) {
            throw new BusinessException("La suspensión ya fue levantada");
        }
        if (!fecha.isAfter(fechaInicio)) {
            throw new BusinessException("El levantamiento debe ser posterior al inicio de la suspensión");
        }
        if (observacion == null || observacion.isBlank()) {
            throw new BusinessException("Indique por qué se levanta la suspensión (condiciones restablecidas)");
        }
        this.fechaFin = fecha;
        this.observacionLevantamiento = observacion.trim();
        this.levantadaPor = quien;
    }
}
