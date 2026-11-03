package com.lau.api.personal;

import com.lau.api.compartido.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Documento que acredita el cumplimiento de requisitos del personal (§4.6, §7.3.1), incluidas las
 * afiliaciones a seguridad social. Dato sensible: solo el rol SUPERVISOR puede consultarlo.
 */
@Getter
@Setter
@Entity
@Table(name = "documento_personal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentoPersonal extends BaseEntity {

    public static final int DIAS_ALERTA_VENCIMIENTO = 30;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoDocumentoPersonal tipo;

    @Column(length = 60)
    private String numero;

    @Column(name = "entidad_emisora", length = 120)
    private String entidadEmisora;

    @Column(name = "fecha_expedicion")
    private LocalDate fechaExpedicion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    /** Solo para documentos mensuales (planilla PILA), formato "2026-11". */
    @Column(length = 7)
    private String periodo;

    /** Archivo en S3. La tabla de adjuntos se agrega en una migración posterior. */
    @Column(name = "adjunto_id")
    private UUID adjuntoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificado_por_id")
    private UsuarioSistema verificadoPor;

    @Column(name = "verificado_en")
    private Instant verificadoEn;

    public DocumentoPersonal(Personal personal, TipoDocumentoPersonal tipo) {
        this.personal = personal;
        this.tipo = tipo;
    }

    public void verificar(UsuarioSistema supervisor) {
        this.verificadoPor = supervisor;
        this.verificadoEn = Instant.now();
    }

    public EstadoDocumento estado(LocalDate hoy) {
        if (fechaVencimiento != null && fechaVencimiento.isBefore(hoy)) {
            return EstadoDocumento.VENCIDO;
        }
        if (verificadoEn == null) {
            return EstadoDocumento.SIN_VERIFICAR;
        }
        if (fechaVencimiento != null && !fechaVencimiento.isAfter(hoy.plusDays(DIAS_ALERTA_VENCIMIENTO))) {
            return EstadoDocumento.POR_VENCER;
        }
        return EstadoDocumento.VIGENTE;
    }
}
