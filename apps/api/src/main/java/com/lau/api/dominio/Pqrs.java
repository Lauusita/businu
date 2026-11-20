package com.lau.api.dominio;

import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pqrs.EstadoPqrs;
import com.lau.api.dominio.enums.pqrs.TipoPqrs;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

/**
 * PQRS radicada por un usuario del servicio desde el formulario público (código QR). El texto del
 * usuario no se modifica nunca; el supervisor solo registra la gestión. Todo cambio queda auditado.
 */
@Getter
@Entity
@Audited
@Table(name = "pqrs", indexes = @Index(name = "ix_pqrs_estado_fecha", columnList = "estado, creado_en"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pqrs extends BaseEntity {

    /** Número que se le muestra al usuario, p. ej. PQRS-2026-000015. */
    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String radicado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, updatable = false)
    private TipoPqrs tipo;

    @Column(nullable = false, columnDefinition = "text", updatable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, updatable = false)
    private Embarcaderos embarcadero;

    @Column(name = "fecha_hecho", updatable = false)
    private LocalDate fechaHecho;

    // ---- Contacto opcional, solo con autorización de tratamiento de datos (Ley 1581 de 2012)

    @Column(length = 150, updatable = false)
    private String nombre;

    @Column(length = 150, updatable = false)
    private String correo;

    @Column(length = 30, updatable = false)
    private String telefono;

    @Column(name = "autoriza_datos", nullable = false, updatable = false)
    private boolean autorizaDatos;

    // ---- Gestión (solo supervisor)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPqrs estado;

    @Column(columnDefinition = "text")
    private String respuesta;

    /** Entidad a la que se trasladó (estado TRASLADADA). */
    @Column(name = "entidad_traslado", length = 200)
    private String entidadTraslado;

    @Column(name = "observacion_interna", columnDefinition = "text")
    private String observacionInterna;

    @Column(name = "fecha_respuesta")
    private Instant fechaRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionado_por_id")
    private Personal gestionadoPor;

    public Pqrs(String radicado, TipoPqrs tipo, String descripcion, Embarcaderos embarcadero, LocalDate fechaHecho,
                String nombre, String correo, String telefono, boolean autorizaDatos) {
        boolean daContacto = !vacio(nombre) || !vacio(correo) || !vacio(telefono);
        if (daContacto && !autorizaDatos) {
            throw new BusinessException("Para dejar sus datos de contacto debe autorizar su tratamiento");
        }
        this.radicado = radicado;
        this.tipo = tipo;
        this.descripcion = descripcion.trim();
        this.embarcadero = embarcadero;
        this.fechaHecho = fechaHecho;
        this.nombre = limpiar(nombre);
        this.correo = limpiar(correo);
        this.telefono = limpiar(telefono);
        this.autorizaDatos = autorizaDatos;
        this.estado = EstadoPqrs.RECIBIDA;
    }

    /** Registra la gestión del supervisor respetando el flujo de estados. */
    public void gestionar(EstadoPqrs nuevo, String respuesta, String entidadTraslado, String observacion,
                          Personal supervisor) {
        if (estado == EstadoPqrs.CERRADA) {
            throw new BusinessException("La PQRS " + radicado + " ya está cerrada");
        }
        if (nuevo == EstadoPqrs.RECIBIDA) {
            throw new BusinessException("Una PQRS no puede volver al estado RECIBIDA");
        }
        if (nuevo == EstadoPqrs.RESPONDIDA && vacio(respuesta)) {
            throw new BusinessException("Registre la respuesta dada al usuario");
        }
        if (nuevo == EstadoPqrs.TRASLADADA && vacio(entidadTraslado)) {
            throw new BusinessException("Indique la entidad a la que se trasladó la PQRS");
        }
        if (nuevo == EstadoPqrs.CERRADA && estado != EstadoPqrs.RESPONDIDA && estado != EstadoPqrs.TRASLADADA) {
            throw new BusinessException("Solo se cierra una PQRS respondida o trasladada");
        }
        if (!vacio(respuesta)) {
            this.respuesta = respuesta.trim();
        }
        if (!vacio(entidadTraslado)) {
            this.entidadTraslado = entidadTraslado.trim();
        }
        if (!vacio(observacion)) {
            this.observacionInterna = observacion.trim();
        }
        if ((nuevo == EstadoPqrs.RESPONDIDA || nuevo == EstadoPqrs.TRASLADADA) && fechaRespuesta == null) {
            this.fechaRespuesta = Instant.now();
        }
        this.estado = nuevo;
        this.gestionadoPor = supervisor;
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String limpiar(String s) {
        return vacio(s) ? null : s.trim();
    }
}
