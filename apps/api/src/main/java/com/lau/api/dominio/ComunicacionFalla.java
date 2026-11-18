package com.lau.api.dominio;

import com.lau.api.dominio.enums.comunicacion.EstadoEnvio;
import com.lau.api.dominio.enums.comunicacion.TipoReferenciaFalla;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Constancia de la comunicación de una falla o novedad al supervisor por correo electrónico
 * (§7.1.12, R50). Nace PENDIENTE; pasa a ENVIADO solo cuando el servidor de correo acepta el mensaje.
 * La fecha de registro es creado_en.
 */
@Getter
@Entity
@Table(name = "comunicaciones_falla",
        indexes = @Index(name = "ix_comunicacion_referencia", columnList = "referencia_tipo, referencia_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComunicacionFalla extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo", nullable = false, length = 20)
    private TipoReferenciaFalla referenciaTipo;

    @Column(name = "referencia_id", nullable = false)
    private UUID referenciaId;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "text")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoEnvio estado;

    /** Momento en que el servidor SMTP aceptó el mensaje. */
    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    /** Message-ID del correo: permite acreditar el envío. */
    @Column(name = "message_id", length = 255)
    private String messageId;

    /** Causa del último fallo de envío. */
    @Column(columnDefinition = "text")
    private String error;

    @Column(nullable = false)
    private int intentos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    public ComunicacionFalla(TipoReferenciaFalla referenciaTipo, UUID referenciaId, String destinatario,
                             String asunto, String mensaje, Personal registradoPor) {
        this.referenciaTipo = referenciaTipo;
        this.referenciaId = referenciaId;
        this.destinatario = destinatario.trim();
        this.asunto = asunto.trim();
        this.mensaje = mensaje.trim();
        this.registradoPor = registradoPor;
        this.estado = EstadoEnvio.PENDIENTE;
    }

    /** Antes de cada intento de envío. Un correo ya enviado no se vuelve a enviar. */
    public void iniciarIntento() {
        if (estado == EstadoEnvio.ENVIADO) {
            throw new BusinessException("La comunicación ya fue enviada");
        }
        intentos++;
    }

    public void marcarEnviado(Instant fecha, String messageId) {
        this.estado = EstadoEnvio.ENVIADO;
        this.fechaEnvio = fecha;
        this.messageId = messageId;
        this.error = null;
    }

    public void marcarFallido(String error) {
        this.estado = EstadoEnvio.FALLIDO;
        this.error = error;
    }
}
