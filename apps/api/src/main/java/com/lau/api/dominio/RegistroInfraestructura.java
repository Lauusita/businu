package com.lau.api.dominio;

import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.*;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Registro de condiciones y mantenimiento de embarcaderos y ayudas a la navegación (§4.4).
 * Cada registro es de UN embarcadero o de UNA ayuda, nunca de ambos.
 */
@Getter
@Entity
@Table(name = "registros_infraestructura", indexes = {
        @Index(name = "ix_reg_infra_embarcadero", columnList = "embarcadero, fecha_hora"),
        @Index(name = "ix_reg_infra_ayuda", columnList = "ayuda_id, fecha_hora"),
        @Index(name = "ix_reg_infra_estado", columnList = "estado")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistroInfraestructura extends BaseEntity {

    public static final String NINGUNA = "Ninguna";

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    // ---- Sobre qué (exactamente uno de los dos) ----

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Embarcaderos embarcadero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ayuda_id")
    private AyudaNavegacion ayuda;

    // ---- Qué se encontró ----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRegistroInfraestructura tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComponenteInfraestructura componente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_observado", nullable = false, length = 30)
    private EstadoActivo estadoObservado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severidad severidad;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "registro_infraestructura_aspectos", joinColumns = @JoinColumn(name = "registro_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "aspecto", nullable = false, length = 20)
    private Set<AspectoAfectado> aspectosAfectados = EnumSet.noneOf(AspectoAfectado.class);

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(name = "acciones_realizadas", nullable = false, columnDefinition = "text")
    private String accionesRealizadas = NINGUNA;

    // ---- Intervención de terceros y seguimiento ----

    @Column(name = "requiere_intervencion_externa", nullable = false)
    private boolean requiereIntervencionExterna;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_competente", nullable = false, length = 30)
    private EntidadCompetente entidadCompetente = EntidadCompetente.NO_APLICA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSeguimiento estado;

    @Embedded
    private ComunicacionSupervisor comunicacion;

    @Column(name = "cerrado_en")
    private Instant fechaCierre;

    @Column(name = "observacion_cierre", columnDefinition = "text")
    private String observacionCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cerrado_por_id")
    private Personal cerradoPor;

    @ManyToMany
    @JoinTable(name = "registro_infraestructura_evidencias",
            joinColumns = @JoinColumn(name = "registro_id"),
            inverseJoinColumns = @JoinColumn(name = "adjunto_id"))
    private List<Adjunto> evidencias = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    public RegistroInfraestructura(Instant fechaHora, Embarcaderos embarcadero, AyudaNavegacion ayuda,
                                   TipoRegistroInfraestructura tipo, ComponenteInfraestructura componente,
                                   EstadoActivo estadoObservado, Severidad severidad,
                                   Set<AspectoAfectado> aspectos, String descripcion, String accionesRealizadas,
                                   boolean requiereIntervencionExterna, EntidadCompetente entidadCompetente,
                                   Personal registradoPor) {
        if ((embarcadero == null) == (ayuda == null)) {
            throw new BusinessException("El registro debe ser de un embarcadero o de una ayuda a la navegación (solo uno)");
        }
        if (ayuda != null && !ayuda.isActiva()) {
            throw new BusinessException("La ayuda " + ayuda.getCodigo() + " está retirada del recorrido");
        }
        if (embarcadero != null && !componente.aplicaAEmbarcadero()) {
            throw new BusinessException(componente + " no es un componente de un embarcadero");
        }
        if (ayuda != null && !componente.aplicaAAyuda()) {
            throw new BusinessException(componente + " no es un componente de una ayuda a la navegación");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new BusinessException("Describa la condición o actividad registrada");
        }
        if (!tipo.esRutinario() && (aspectos == null || aspectos.isEmpty())) {
            throw new BusinessException("Indique a qué afecta: operación, navegación, accesibilidad o seguridad");
        }
        if (requiereIntervencionExterna
                && (entidadCompetente == null || entidadCompetente == EntidadCompetente.NO_APLICA)) {
            throw new BusinessException("Indique la entidad competente que debe intervenir");
        }
        if (ayuda != null && componente.requiereLuz() && !ayuda.isLuminosa()) {
            throw new BusinessException("La ayuda " + ayuda.getCodigo() + " no tiene sistema de iluminación");
        }

        this.fechaHora = fechaHora;
        this.embarcadero = embarcadero;
        this.ayuda = ayuda;
        this.tipo = tipo;
        this.componente = componente;
        this.estadoObservado = estadoObservado;
        this.severidad = severidad;
        if (aspectos != null && !aspectos.isEmpty()) {
            this.aspectosAfectados.addAll(aspectos);
        }
        this.descripcion = descripcion.trim();
        this.accionesRealizadas = textoOPorDefecto(accionesRealizadas, NINGUNA);
        this.requiereIntervencionExterna = requiereIntervencionExterna;
        this.entidadCompetente = requiereIntervencionExterna ? entidadCompetente : EntidadCompetente.NO_APLICA;
        this.registradoPor = registradoPor;

        // Revisión o mantenimiento rutinario sin terceros: queda cerrado; lo demás requiere seguimiento
        boolean sinSeguimiento = tipo.esRutinario() && !requiereIntervencionExterna;
        this.estado = sinSeguimiento ? EstadoSeguimiento.CERRADO : EstadoSeguimiento.ABIERTO;
        if (sinSeguimiento) {
            this.fechaCierre = fechaHora;
            this.cerradoPor = registradoPor;
            this.observacionCierre = "Registro rutinario sin seguimiento";
        }
    }

    // ---- Transiciones ----

    /** §4.4: constancia de que se informó al supervisor del contrato. */
    public void comunicarSupervisor(Instant fecha, MedioComunicacion medio, String referencia, Personal quien) {
        if (estado != EstadoSeguimiento.ABIERTO) {
            throw new BusinessException("Solo se comunican registros abiertos (estado actual: " + estado + ")");
        }
        if (fecha.isBefore(fechaHora)) {
            throw new BusinessException("La comunicación no puede ser anterior al registro");
        }
        this.comunicacion = new ComunicacionSupervisor(fecha, medio, textoOPorDefecto(referencia, NINGUNA), quien);
        this.estado = EstadoSeguimiento.COMUNICADO;
    }

    public void cerrar(Instant fecha, String observacion, Personal quien) {
        if (estado == EstadoSeguimiento.CERRADO) {
            throw new BusinessException("El registro ya está cerrado");
        }
        if (requiereIntervencionExterna && estado != EstadoSeguimiento.COMUNICADO) {
            throw new BusinessException("Antes de cerrar, deje constancia de la comunicación al supervisor del contrato");
        }
        if (observacion == null || observacion.isBlank()) {
            throw new BusinessException("Indique cómo se resolvió o por qué se cierra");
        }
        this.fechaCierre = fecha;
        this.observacionCierre = observacion.trim();
        this.cerradoPor = quien;
        this.estado = EstadoSeguimiento.CERRADO;
    }

    public void agregarEvidencia(Adjunto adjunto) {
        evidencias.add(adjunto);
    }

    // ---- Consultas ----

    /** Pendiente de comunicar: requiere a otra entidad y el supervisor aún no ha sido informado. */
    public boolean pendienteDeComunicar() {
        return requiereIntervencionExterna && estado == EstadoSeguimiento.ABIERTO;
    }

    public Set<AspectoAfectado> getAspectosAfectados() {
        return Collections.unmodifiableSet(aspectosAfectados);
    }

    public List<Adjunto> getEvidencias() {
        return Collections.unmodifiableList(evidencias);
    }

    private static String textoOPorDefecto(String valor, String porDefecto) {
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }
}