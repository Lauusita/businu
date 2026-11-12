package com.lau.api.dominio;

import com.lau.api.dominio.enums.*;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Zarpe de una embarcación: bitácora de viaje (§4.1 del anexo). Una operación tiene varios
 * registros de usuarios transportados (uno por cada origen/destino de pasajeros).
 */
@Getter
@Entity
@Table(name = "operaciones", indexes = @Index(name = "ix_operaciones_inicio", columnList = "fecha_hora_inicio"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Operacion extends BaseEntity {

    public static final String SIN_NOVEDADES = "Sin novedades";
    public static final String SIN_OBSERVACIONES = "Sin observaciones";
    public static final String NO_APLICA = "No aplica";

    @Column(nullable = false, columnDefinition = "text")
    private String novedades = SIN_NOVEDADES;

    @Column(nullable = false, columnDefinition = "text")
    private String observaciones = SIN_OBSERVACIONES;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "embarcadero_origen", nullable = false, length = 30)
    private Embarcaderos embarcaderoOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "embarcadero_destino", nullable = false, length = 30)
    private Embarcaderos embarcaderoDestino;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private Instant fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private Instant fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado;

    // ---- Antes de zarpar ----

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nave", nullable = false, length = 10)
    private EstadoNave estadoNave;

    @Embedded
    private VerificacionPrevia verificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_climatica", nullable = false, length = 30)
    private CondicionClimatica condicionClimatica;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_rio", nullable = false, length = 10)
    private NivelRio nivelRio;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "operacion_condiciones_mecanicas", joinColumns = @JoinColumn(name = "operacion_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "condicion", nullable = false, length = 40)
    private Set<CondicionesMecanicas> condicionesMecanicas = EnumSet.of(CondicionesMecanicas.SIN_NOVEDAD);

    /** Obligatorio si alguna condición es OTRA. */
    @Column(name = "detalle_condiciones_mecanicas", nullable = false, columnDefinition = "text")
    private String detalleCondicionesMecanicas = NO_APLICA;

    @Column(name = "justificacion_verificacion", nullable = false, columnDefinition = "text")
    private String justificacionVerificacion = NO_APLICA;

    @ManyToMany
    @JoinTable(name = "operacion_tripulacion",
            joinColumns = @JoinColumn(name = "operacion_id"),
            inverseJoinColumns = @JoinColumn(name = "personal_id"))
    private Set<Personal> tripulacion = new HashSet<>();

    // ---- Durante y al finalizar ----

    @ElementCollection
    @CollectionTable(name = "operacion_paradas", joinColumns = @JoinColumn(name = "operacion_id"))
    @OrderColumn(name = "orden")
    private List<Parada> paradas = new ArrayList<>();
    
    @Column(name = "motivo_suspension", columnDefinition = "text")
    private String motivoSuspension;

    /** Horómetro al finalizar (alimenta el conteo de 100 h de los motores). */
    
    @Column(name = "horometro_final", precision = 10, scale = 1)
    private BigDecimal horometroFinal;

    
    @OneToMany(mappedBy = "operacion")
    private List<Usuario> usuarios = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;
    public Operacion(Embarcacion embarcacion, Embarcaderos origen, Embarcaderos destino, Instant inicio,
                     EstadoNave estadoNave, VerificacionPrevia verificacion, String justificacionVerificacion,
                     CondicionClimatica clima, NivelRio nivelRio, Set<CondicionesMecanicas> condicionesMecanicas,
                     String detalleCondicionesMecanicas, Personal registradoPor) {
        if (origen == destino) {
            throw new BusinessException("El embarcadero de origen y el de destino deben ser distintos");
        }
        if (estadoNave == EstadoNave.MALO) {
            throw new BusinessException("La embarcación no puede zarpar en estado MALO");
        }
        if (!verificacion.isChalecosSalvavidas()) {
            throw new BusinessException(
                    "No es posible zarpar si la embarcación no cuenta con los chalecos salvavidas requeridos");
        }
        if (!verificacion.todoConforme()
                && (justificacionVerificacion == null || justificacionVerificacion.isBlank())) {
            throw new BusinessException(
                    "Hay puntos de la verificación previa sin cumplir: indique la justificación para zarpar");
        }
        if (condicionesMecanicas.contains(CondicionesMecanicas.OTRA)
                && (detalleCondicionesMecanicas == null || detalleCondicionesMecanicas.isBlank())) {
            throw new BusinessException("Debe especificar el detalle de la condición mecánica.");
        }

        this.embarcacion = embarcacion;
        this.embarcaderoOrigen = origen;
        this.embarcaderoDestino = destino;
        this.fechaHoraInicio = inicio;
        this.estadoNave = estadoNave;
        this.verificacion = verificacion;
        this.justificacionVerificacion = verificacion.todoConforme()
                ? NO_APLICA
                : justificacionVerificacion.trim();
        this.condicionClimatica = clima;
        this.nivelRio = nivelRio;
        this.registradoPor = registradoPor;
        this.estado = EstadoOperacion.EN_CURSO;
        this.condicionesMecanicas = condicionesMecanicas;
        this.detalleCondicionesMecanicas = condicionesMecanicas.contains(CondicionesMecanicas.OTRA)
                ? detalleCondicionesMecanicas.trim()
                : NO_APLICA;
        this.paradas.add(new Parada(origen, null, inicio));
    }

    /** Actualiza la bitácora mientras el viaje está en curso. Un campo en null no se modifica. */
    public void actualizarBitacora(CondicionClimatica clima, NivelRio nivelRio, String novedades,
                                   String observaciones) {
        exigirEnCurso();
        if (clima != null) this.condicionClimatica = clima;
        if (nivelRio != null) this.nivelRio = nivelRio;
        if (novedades != null) this.novedades = textoOPorDefecto(novedades, SIN_NOVEDADES);
        if (observaciones != null) this.observaciones = textoOPorDefecto(observaciones, SIN_OBSERVACIONES);
    }

    /** Reemplaza las condiciones mecánicas reportadas. Vacío = SIN_NOVEDAD. */
    public void registrarCondicionesMecanicas(Set<CondicionesMecanicas> condiciones, String detalle) {
        exigirEnCurso();
        Set<CondicionesMecanicas> nuevas = (condiciones == null || condiciones.isEmpty())
                ? EnumSet.of(CondicionesMecanicas.SIN_NOVEDAD)
                : EnumSet.copyOf(condiciones);
        if (nuevas.contains(CondicionesMecanicas.SIN_NOVEDAD) && nuevas.size() > 1) {
            throw new BusinessException("SIN_NOVEDAD no puede combinarse con otras condiciones");
        }
        boolean requiereDetalle = nuevas.contains(CondicionesMecanicas.OTRA);
        if (requiereDetalle && (detalle == null || detalle.isBlank())) {
            throw new BusinessException("Describa la condición mecánica marcada como OTRA");
        }
        this.condicionesMecanicas.clear();
        this.condicionesMecanicas.addAll(nuevas);
        this.detalleCondicionesMecanicas = textoOPorDefecto(detalle, NO_APLICA);
    }

    public Set<CondicionesMecanicas> getCondicionesMecanicas() {
        return Collections.unmodifiableSet(condicionesMecanicas);
    }

    private static String textoOPorDefecto(String valor, String porDefecto) {
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }

    public boolean esTripulante(Personal persona) {
        return tripulacion.contains(persona);
    }


    public void agregarTripulante(Personal persona) {
        if (persona.getRol() != Rol.OPERADOR_EMBARCACION && persona.getRol() != Rol.GUIA_TURISTICA) {
            throw new BusinessException("La tripulación solo incluye operadores de embarcación y guías");
        }
        tripulacion.add(persona);
    }

    /** Parada intermedia (p. ej. Centro – Calle 22). */
    public void registrarParada(Embarcaderos embarcadero, Instant llegada, Instant salida) {
        exigirEnCurso();
        if (!estaEnRecorrido(embarcadero) || embarcadero == embarcaderoOrigen || embarcadero == embarcaderoDestino) {
            throw new BusinessException(embarcadero + " no es una parada intermedia de este recorrido");
        }
        if (paradas.stream().anyMatch(p -> p.getEmbarcadero() == embarcadero)) {
            throw new BusinessException("La parada en " + embarcadero + " ya fue registrada");
        }
        if (!llegada.isAfter(fechaHoraInicio) || salida.isBefore(llegada)) {
            throw new BusinessException("Horas de parada inválidas: llegada después del inicio y salida después de la llegada");
        }
        paradas.add(new Parada(embarcadero, llegada, salida));
    }

    public void finalizar(Instant fin, BigDecimal horometroFinal) {
        exigirEnCurso();
        if (!fin.isAfter(fechaHoraInicio)) {
            throw new BusinessException("La hora de finalización debe ser posterior a la de inicio");
        }
        if (totalUsuariosTransportados() == 0) {
            throw new BusinessException("La cantidad total de usuarios transportados no puede ser igual a 0. Registra los usuarios para poder finalizar.");
        }
        paradas.add(new Parada(embarcaderoDestino, fin, null));
        this.fechaHoraFin = fin;
        this.horometroFinal = horometroFinal;
        this.estado = EstadoOperacion.FINALIZADA;
    }

    /** §7.1.7: suspensión por clima, nivel del río, autoridad, falla mecánica, etc. */
    public void suspender(Instant momento, String motivo) {
        exigirEnCurso();
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessException("Indique el motivo de la suspensión");
        }
        this.fechaHoraFin = momento;
        this.motivoSuspension = motivo;
        this.estado = EstadoOperacion.SUSPENDIDA;
    }

    /** Lo llama el constructor de Usuario: mantiene la lista sincronizada en memoria. */
    void agregarUsuario(Usuario usuario) {
        if (estado == EstadoOperacion.SUSPENDIDA) {
            throw new BusinessException("No se pueden registrar usuarios en una operación suspendida");
        }
        usuarios.add(usuario);
    }

    /** Cantidad de pasajeros transportados en el viaje (§4.1): suma de los registros de usuarios. */
    public int totalUsuariosTransportados() {
        return usuarios.stream().mapToInt(Usuario::getUsuariosTransportados).sum();
    }

    public List<Parada> getParadas() {
        return Collections.unmodifiableList(paradas);
    }

    public Set<Personal> getTripulacion() {
        return Collections.unmodifiableSet(tripulacion);
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    private void exigirEnCurso() {
        if (estado != EstadoOperacion.EN_CURSO) {
            throw new BusinessException("La operación ya está " + estado);
        }
    }

    /** Se aborda en cualquier embarcadero del recorrido, menos en el destino final. */
    public boolean admiteAbordajeEn(Embarcaderos embarcadero) {
        return estaEnRecorrido(embarcadero) && embarcadero != embarcaderoDestino;
    }

    /** El destino debe estar más adelante que el abordaje, en el sentido de la operación. */
    public boolean esDestinoValido(Embarcaderos abordaje, Embarcaderos destino) {
        if (!estaEnRecorrido(destino)) {
            return false;
        }
        int sentido = Integer.signum(embarcaderoDestino.ordinal() - embarcaderoOrigen.ordinal());
        return Integer.signum(destino.ordinal() - abordaje.ordinal()) == sentido;
    }

    private boolean estaEnRecorrido(Embarcaderos e) {
        int min = Math.min(embarcaderoOrigen.ordinal(), embarcaderoDestino.ordinal());
        int max = Math.max(embarcaderoOrigen.ordinal(), embarcaderoDestino.ordinal());
        return e.ordinal() >= min && e.ordinal() <= max;
    }
}