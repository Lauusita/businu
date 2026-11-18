package com.lau.api.dominio;

import com.lau.api.dominio.enums.mantenimiento.SistemaIntervenido;
import com.lau.api.dominio.enums.mantenimiento.TipoMantenimiento;
import com.lau.api.dominio.enums.personal.Rol;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Intervención registrada en la hoja de vida de una embarcación (§4.3, §7.1.8).
 * La hoja de vida es la consulta de la embarcación con sus motores y sus mantenimientos.
 */
@Getter
@Entity
@Table(name = "mantenimientos",
        indexes = @Index(name = "ix_mantenimiento_embarcacion_fecha", columnList = "embarcacion_id, fecha_intervencion"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mantenimiento extends BaseEntity {

    public static final String NO_ESPECIFICADO = "No especificado";
    public static final String NO_APLICA = "No aplica";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    /** Solo cuando el sistema intervenido es MOTOR. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motor_id")
    private Motor motor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMantenimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SistemaIntervenido sistema;

    @Column(name = "fecha_intervencion", nullable = false)
    private Instant fechaIntervencion;

    @Column(name = "descripcion_actividades", nullable = false, columnDefinition = "text")
    private String descripcionActividades;

    /** Técnico o taller que ejecutó el trabajo. */
    @Column(name = "ejecutado_por", nullable = false, length = 150)
    private String ejecutadoPor;

    /** Piloto (operador de embarcación) al que está asignada la intervención. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Personal responsable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    // ---- Repuestos: lista o "No aplica / Ninguno" ----

    @Column(name = "sin_repuestos", nullable = false)
    private boolean sinRepuestos;

    @ElementCollection
    @CollectionTable(name = "mantenimiento_repuestos", joinColumns = @JoinColumn(name = "mantenimiento_id"))
    private List<RepuestoUtilizado> repuestos = new ArrayList<>();

    // ---- Novedades: lista o "No aplica / Ninguna" ----

    @Column(name = "sin_novedades", nullable = false)
    private boolean sinNovedades;

    @ElementCollection
    @CollectionTable(name = "mantenimiento_novedades", joinColumns = @JoinColumn(name = "mantenimiento_id"))
    private List<NovedadMecanica> novedades = new ArrayList<>();

    // ---- Recomendaciones (0..3, una por tipo) ----

    @ElementCollection
    @CollectionTable(name = "mantenimiento_recomendaciones", joinColumns = @JoinColumn(name = "mantenimiento_id"))
    private List<RecomendacionMantenimiento> recomendaciones = new ArrayList<>();

    // ---- Próxima intervención (§4.3: fecha o condición estimada) ----

    /** Fecha estimada; puede no conocerse. Cuando exista el horómetro se calculará por las 100 h. */
    @Column(name = "proxima_intervencion")
    private Instant proximaIntervencion;

    /** Condición estimada, p. ej. "al cumplir 100 h de motor". "No aplica" si no hay. */
    @Column(name = "condicion_proxima_intervencion", nullable = false, length = 300)
    private String condicionProximaIntervencion;

    /** Costo de mano de obra en COP (0 si fue personal propio). */
    @Column(name = "costo_mano_obra", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoManoObra;

    // ---- Soportes: facturas, fotos (Adjunto ya vinculado) ----

    @ManyToMany
    @JoinTable(name = "mantenimiento_soportes",
            joinColumns = @JoinColumn(name = "mantenimiento_id"),
            inverseJoinColumns = @JoinColumn(name = "adjunto_id"))
    private List<Adjunto> soportes = new ArrayList<>();

    public Mantenimiento(Embarcacion embarcacion, Motor motor, TipoMantenimiento tipo, SistemaIntervenido sistema,
                         Instant fechaIntervencion, String descripcionActividades, String ejecutadoPor,
                         Personal responsable, Personal registradoPor, BigDecimal costoManoObra) {
        if (sistema == SistemaIntervenido.MOTOR && motor == null) {
            throw new BusinessException("Indique cuál motor se intervino");
        }
        if (motor != null && !motor.getEmbarcacion().getId().equals(embarcacion.getId())) {
            throw new BusinessException("El motor " + motor.getSerial() + " no pertenece a " + embarcacion.getNombre());
        }
        if (descripcionActividades == null || descripcionActividades.isBlank()) {
            throw new BusinessException("Describa las actividades ejecutadas");
        }
        if (responsable.getRol() != Rol.OPERADOR_EMBARCACION) {
            throw new BusinessException("El responsable del mantenimiento debe ser un operador de embarcación");
        }
        // Contrato, no disponibilidad: un operador puede estar navegando en la otra embarcación
        if (!responsable.isContratoActivo()) {
            throw new BusinessException("El responsable del mantenimiento no tiene contrato activo");
        }
        if (costoManoObra != null && costoManoObra.signum() < 0) {
            throw new BusinessException("El costo de mano de obra no puede ser negativo");
        }
        this.embarcacion = embarcacion;
        this.motor = motor;
        this.tipo = tipo;
        this.sistema = sistema;
        this.fechaIntervencion = fechaIntervencion;
        this.descripcionActividades = descripcionActividades.trim();
        this.ejecutadoPor = (ejecutadoPor == null || ejecutadoPor.isBlank()) ? NO_ESPECIFICADO : ejecutadoPor.trim();
        this.responsable = responsable;
        this.registradoPor = registradoPor;
        this.costoManoObra = costoManoObra == null ? BigDecimal.ZERO : costoManoObra;
        this.condicionProximaIntervencion = NO_APLICA;
    }

    // ---- Repuestos y novedades ----

    /** null o vacío = "No aplica / Ninguno". */
    public void registrarRepuestos(List<RepuestoUtilizado> lista) {
        repuestos.clear();
        if (lista != null) {
            repuestos.addAll(lista);
        }
        sinRepuestos = repuestos.isEmpty();
    }

    /** null o vacío = "No aplica / Ninguna". */
    public void registrarNovedades(List<NovedadMecanica> lista) {
        novedades.clear();
        novedades.addAll(NovedadMecanica.validar(lista));
        sinNovedades = novedades.isEmpty();
    }

    /** Una recomendación por tipo: si se repite el tipo, se rechaza. */
    public void registrarRecomendaciones(List<RecomendacionMantenimiento> lista) {
        recomendaciones.clear();
        if (lista == null) {
            return;
        }
        for (RecomendacionMantenimiento r : lista) {
            boolean repetida = recomendaciones.stream().anyMatch(x -> x.getTipo() == r.getTipo());
            if (repetida) {
                throw new BusinessException("Solo puede haber una recomendación " + r.getTipo());
            }
            recomendaciones.add(r);
        }
    }

    public void programarProximaIntervencion(Instant fecha, String condicion) {
        if (fecha != null && !fecha.isAfter(fechaIntervencion)) {
            throw new BusinessException("La próxima intervención debe ser posterior a la fecha de esta intervención");
        }
        this.proximaIntervencion = fecha;
        this.condicionProximaIntervencion = (condicion == null || condicion.isBlank()) ? NO_APLICA : condicion.trim();
    }

    public void agregarSoporte(Adjunto adjunto) {
        soportes.add(adjunto);
    }

    // ---- Consultas ----

    public BigDecimal costoRepuestos() {
        return repuestos.stream().map(RepuestoUtilizado::getCosto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal costoTotal() {
        return costoRepuestos().add(costoManoObra);
    }

    public List<RepuestoUtilizado> getRepuestos() {
        return Collections.unmodifiableList(repuestos);
    }

    public List<NovedadMecanica> getNovedades() {
        return Collections.unmodifiableList(novedades);
    }

    public List<RecomendacionMantenimiento> getRecomendaciones() {
        return Collections.unmodifiableList(recomendaciones);
    }

    public List<Adjunto> getSoportes() {
        return Collections.unmodifiableList(soportes);
    }
}