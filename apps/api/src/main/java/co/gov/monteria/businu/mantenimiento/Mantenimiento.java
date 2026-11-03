package co.gov.monteria.businu.mantenimiento;

import co.gov.monteria.businu.catalogos.CatalogoItem;
import co.gov.monteria.businu.catalogos.TipoCatalogo;
import co.gov.monteria.businu.compartido.BaseEntity;
import co.gov.monteria.businu.compartido.ReglaNegocioException;
import co.gov.monteria.businu.flota.Embarcacion;
import co.gov.monteria.businu.flota.Motor;
import co.gov.monteria.businu.personal.Cargo;
import co.gov.monteria.businu.personal.Personal;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Intervención registrada en la hoja de vida de una embarcación (§4.3, §7.1.8).
 *
 * <p>La hoja de vida no es una tabla: es la consulta de la embarcación con sus motores, sus
 * mantenimientos y lo registrado en ellos.
 */
@Getter
@Setter
@Entity
@Table(name = "mantenimiento")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mantenimiento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMantenimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SistemaIntervenido sistema;

    @Column(name = "fecha_intervencion", nullable = false)
    private LocalDate fechaIntervencion;

    @Column(name = "descripcion_actividades", nullable = false, columnDefinition = "text")
    private String descripcionActividades;

    /** Técnico o taller que ejecutó el trabajo. */
    @Column(name = "ejecutado_por", length = 120)
    private String ejecutadoPor;

    /** Piloto (conductor) al que está asignada la intervención. Quien la registra queda en creado_por. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Personal responsable;

    @Column(name = "costo_mano_obra", precision = 14, scale = 2)
    private BigDecimal costoManoObra;

    /** "No aplica / Ninguno": distingue "no se usaron repuestos" de "no se diligenció". */
    @Column(name = "sin_repuestos", nullable = false)
    @Setter(AccessLevel.NONE)
    private boolean sinRepuestos;

    @Column(name = "sin_novedades", nullable = false)
    @Setter(AccessLevel.NONE)
    private boolean sinNovedades;

    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<MantenimientoMotor> motores = new ArrayList<>();

    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<MantenimientoRepuesto> repuestos = new ArrayList<>();

    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<MantenimientoNovedad> novedades = new ArrayList<>();

    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<MantenimientoRecomendacion> recomendaciones = new ArrayList<>();

    public Mantenimiento(Embarcacion embarcacion, TipoMantenimiento tipo, SistemaIntervenido sistema,
                         LocalDate fechaIntervencion, String descripcionActividades, Personal responsable) {
        this.embarcacion = embarcacion;
        this.tipo = tipo;
        this.sistema = sistema;
        this.fechaIntervencion = fechaIntervencion;
        this.descripcionActividades = descripcionActividades;
        this.responsable = responsable;
    }

    // ---- Motores intervenidos -------------------------------------------------------------

    /**
     * @param servicioPeriodico true si es el servicio de 100 h: reinicia el conteo y fija el
     *                          próximo horómetro. Una reparación correctiva no lo reinicia.
     */
    public MantenimientoMotor agregarMotor(Motor motor, BigDecimal horometro, boolean servicioPeriodico) {
        if (!motor.getEmbarcacion().equals(embarcacion)) {
            throw new ReglaNegocioException("El motor " + motor.getSerial() + " no pertenece a la embarcación");
        }
        if (motores.stream().anyMatch(m -> m.getMotor().equals(motor))) {
            throw new ReglaNegocioException("El motor " + motor.getSerial() + " ya está en esta intervención");
        }
        BigDecimal proximo = servicioPeriodico ? motor.proximoServicioDesde(horometro) : null;
        MantenimientoMotor mm = new MantenimientoMotor(this, motor, horometro, servicioPeriodico, proximo);
        motores.add(mm);
        motor.actualizarSiEsMayor(horometro);
        return mm;
    }

    // ---- Repuestos e insumos ----------------------------------------------------------------

    public void marcarSinRepuestos() {
        repuestos.clear();
        sinRepuestos = true;
    }

    public MantenimientoRepuesto agregarRepuesto(CatalogoItem item, BigDecimal cantidad, String unidad, BigDecimal costo) {
        exigirTipo(item, TipoCatalogo.REPUESTO_INSUMO);
        return agregarRepuesto(new MantenimientoRepuesto(this, item, null, cantidad, unidad, costo));
    }

    public MantenimientoRepuesto agregarRepuestoOtro(String descripcion, BigDecimal cantidad, String unidad, BigDecimal costo) {
        return agregarRepuesto(new MantenimientoRepuesto(this, null, descripcion, cantidad, unidad, costo));
    }

    private MantenimientoRepuesto agregarRepuesto(MantenimientoRepuesto r) {
        sinRepuestos = false;
        repuestos.add(r);
        return r;
    }

    // ---- Novedades --------------------------------------------------------------------------

    public void marcarSinNovedades() {
        novedades.clear();
        sinNovedades = true;
    }

    public MantenimientoNovedad agregarNovedad(CatalogoItem item, Severidad severidad, String detalle) {
        exigirTipo(item, TipoCatalogo.NOVEDAD);
        return agregarNovedad(new MantenimientoNovedad(this, item, null, severidad, detalle));
    }

    public MantenimientoNovedad agregarNovedadOtra(String descripcion, Severidad severidad, String detalle) {
        return agregarNovedad(new MantenimientoNovedad(this, null, descripcion, severidad, detalle));
    }

    private MantenimientoNovedad agregarNovedad(MantenimientoNovedad n) {
        sinNovedades = false;
        novedades.add(n);
        return n;
    }

    // ---- Recomendaciones --------------------------------------------------------------------

    /** Un check por tipo: si ya existe la recomendación de ese tipo, se reemplaza su descripción. */
    public void recomendar(TipoRecomendacion tipo, String descripcion) {
        recomendaciones.stream()
                .filter(r -> r.getTipo() == tipo)
                .findFirst()
                .ifPresentOrElse(r -> r.setDescripcion(descripcion),
                        () -> recomendaciones.add(new MantenimientoRecomendacion(this, tipo, descripcion)));
    }

    public void quitarRecomendacion(TipoRecomendacion tipo) {
        recomendaciones.removeIf(r -> r.getTipo() == tipo);
    }

    public List<MantenimientoMotor> getMotores() {
        return Collections.unmodifiableList(motores);
    }

    public List<MantenimientoRepuesto> getRepuestos() {
        return Collections.unmodifiableList(repuestos);
    }

    public List<MantenimientoNovedad> getNovedades() {
        return Collections.unmodifiableList(novedades);
    }

    public List<MantenimientoRecomendacion> getRecomendaciones() {
        return Collections.unmodifiableList(recomendaciones);
    }

    // ---- Validación antes de guardar -------------------------------------------------------

    @PrePersist
    @PreUpdate
    void validar() {
        if (responsable.getCargo() != Cargo.CONDUCTOR) {
            throw new ReglaNegocioException("El responsable del mantenimiento debe ser un piloto (cargo CONDUCTOR)");
        }
        if (sistema == SistemaIntervenido.MOTOR && motores.isEmpty()) {
            throw new ReglaNegocioException("Una intervención al MOTOR debe indicar al menos un motor y su horómetro");
        }
        if (sistema != SistemaIntervenido.MOTOR && !motores.isEmpty()) {
            throw new ReglaNegocioException("Solo las intervenciones al MOTOR registran motores y horómetro");
        }
        if (sinRepuestos == !repuestos.isEmpty()) {
            throw new ReglaNegocioException("Marque los repuestos utilizados o 'No aplica / Ninguno' (no ambos)");
        }
        if (sinNovedades == !novedades.isEmpty()) {
            throw new ReglaNegocioException("Marque las novedades identificadas o 'No aplica / Ninguna' (no ambos)");
        }
    }

    private static void exigirTipo(CatalogoItem item, TipoCatalogo esperado) {
        if (item.getTipo() != esperado) {
            throw new ReglaNegocioException("El ítem '" + item.getNombre() + "' no es de tipo " + esperado);
        }
        if (!item.isActivo()) {
            throw new ReglaNegocioException("El ítem '" + item.getNombre() + "' está inactivo");
        }
    }
}
