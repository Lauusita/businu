package com.lau.api.dominio;

import com.lau.api.dominio.enums.personal.TipoDocumentoPersonal;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * Documento que acredita los requisitos de una persona (§4.6). El PDF está en S3 en
 * documentacion/{ROL}/{cédula}/{TIPO}/... Al subir uno nuevo del mismo tipo (y mes, si es periódico),
 * el anterior deja de estar vigente pero se conserva como historial.
 */
@Getter
@Entity
@Audited
@Table(name = "documentos_personal",
        indexes = @Index(name = "ix_documento_personal_tipo", columnList = "personal_id, tipo, periodo"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentoPersonal extends BaseEntity {

    public static final String SIN_OBSERVACIONES = "Sin observaciones";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoDocumentoPersonal tipo;

    /** Primer día del mes al que corresponde (solo planilla PILA). */
    @Column
    private LocalDate periodo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adjunto_id", nullable = false, unique = true)
    private Adjunto adjunto;

    @Column(nullable = false)
    private boolean vigente;

    @Column(nullable = false, columnDefinition = "text")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    public DocumentoPersonal(Personal personal, TipoDocumentoPersonal tipo, YearMonth periodo, Adjunto adjunto,
                             String observaciones, Personal registradoPor) {
        if (tipo.isPorPeriodo() && periodo == null) {
            throw new BusinessException("Indique el mes al que corresponde la " + tipo.getDescripcion());
        }
        if (!tipo.isPorPeriodo() && periodo != null) {
            throw new BusinessException("El documento " + tipo.getDescripcion() + " no lleva mes");
        }
        if (tipo == TipoDocumentoPersonal.LICENCIA_TRIPULANTE
                && personal.getRol() != com.lau.api.dominio.enums.personal.Rol.OPERADOR_EMBARCACION) {
            throw new BusinessException("La licencia de tripulante solo aplica a operadores de embarcación");
        }
        this.personal = personal;
        this.tipo = tipo;
        this.periodo = periodo == null ? null : periodo.atDay(1);
        this.adjunto = adjunto;
        this.vigente = true;
        this.observaciones = (observaciones == null || observaciones.isBlank()) ? SIN_OBSERVACIONES : observaciones.trim();
        this.registradoPor = registradoPor;
    }

    /** Lo reemplaza un documento más reciente del mismo tipo: queda en el historial. */
    public void reemplazar() {
        this.vigente = false;
    }
}
