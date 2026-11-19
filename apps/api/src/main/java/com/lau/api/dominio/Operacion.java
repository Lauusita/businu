package com.lau.api.dominio;

import com.lau.api.dominio.enums.operacion.*;
import com.lau.api.dominio.enums.personal.Rol;
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

    @Column(name = "cargue_combustible", nullable = false)
    private boolean cargueCombustible;

    /** Novedades mecánicas del viaje. Vacía = sin novedad. */
    @ElementCollection
    @CollectionTable(name = "operacion_condiciones_mecanicas", joinColumns = @JoinColumn(name = "operacion_id"))
    private List<NovedadMecanica> condicionesMecanicas = new ArrayList<>();

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

    /** Texto del motivo (se conserva para compatibilidad); la causa tipificada está en motivo. */
    @Column(name = "motivo_suspension", columnDefinition = "text")
    private String motivoSuspension;

    /** Causa tipificada de la suspensión (R48). Null si el viaje no fue suspendido. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "causa", column = @Column(name = "causa_suspension", length = 40)),
            @AttributeOverride(name = "detalle", column = @Column(name = "detalle_suspension", columnDefinition = "text")),
            @AttributeOverride(name = "autoridad", column = @Column(name = "autoridad_suspension", length = 150))
    })
    private MotivoSuspension motivo;

    /** Horómetro de cada motor al zarpar y al terminar (R37). */
    @ElementCollection
    @CollectionTable(name = "operacion_horometros", joinColumns = @JoinColumn(name = "operacion_id"))
    private List<LecturaHorometro> lecturasHorometro = new ArrayList<>();

    /** Abastecimientos hechos antes del zarpe y asociados a este viaje (§4.1, R16). */
    @OneToMany(mappedBy = "operacion")
    private List<AbastecimientoCombustible> abastecimientos = new ArrayList<>();

    /** Campo heredado: una sola lectura sin indicar el motor. Las lecturas por motor están en lecturasHorometro. */
    @Column(name = "horometro_final", precision = 10, scale = 1)
    private BigDecimal horometroFinal;

    @OneToMany(mappedBy = "operacion")
    private List<Usuario> usuarios = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    public Operacion(Embarcacion embarcacion, Embarcaderos origen, Embarcaderos destino, Instant inicio,
                     EstadoNave estadoNave, VerificacionPrevia verificacion, String justificacionVerificacion,
                     CondicionClimatica clima, NivelRio nivelRio, List<NovedadMecanica> condicionesMecanicas,
                     Personal registradoPor) {
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
        this.condicionesMecanicas.addAll(NovedadMecanica.validar(condicionesMecanicas));
        this.paradas.add(new Parada(origen, null, inicio));
        this.cargueCombustible = false;
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

    /** Reemplaza las novedades mecánicas reportadas. Lista vacía = sin novedad. */
    public void registrarCondicionesMecanicas(List<NovedadMecanica> novedades) {
        exigirEnCurso();
        List<NovedadMecanica> nuevas = NovedadMecanica.validar(novedades);
        this.condicionesMecanicas.clear();
        this.condicionesMecanicas.addAll(nuevas);
    }

    public List<NovedadMecanica> getCondicionesMecanicas() {
        return Collections.unmodifiableList(condicionesMecanicas);
    }

    public boolean sinNovedadesMecanicas() {
        return condicionesMecanicas.isEmpty();
    }

    public static String textoOPorDefecto(String valor, String porDefecto) {
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }

    public boolean esTripulante(Personal persona) {
        return tripulacion.contains(persona);
    }

    public long cantidadDeOperadores() {
        return tripulacion.stream().filter(p -> p.getRol() == Rol.OPERADOR_EMBARCACION).count();
    }

    public void agregarTripulante(Personal persona) {
        exigirEnCurso();   // una operación cerrada no cambia de tripulación (si no, la persona queda sin disponibilidad)
        if (persona.getRol() != Rol.OPERADOR_EMBARCACION && persona.getRol() != Rol.GUIA_TURISTICA) {
            throw new BusinessException("La tripulación solo incluye operadores de embarcación y guías");
        }
        tripulacion.add(persona);
    }

    /**
     * Quita a un tripulante validando ANTES de modificar: la operación en curso conserva 2 operadores
     * y al menos un guía. La disponibilidad de la persona la devuelve el servicio.
     */
    public void eliminarTripulante(Personal persona) {
        exigirEnCurso();
        if (!esTripulante(persona)) {
            throw new BusinessException("La persona no hace parte de la tripulación");
        }
        if (persona.getRol() == Rol.OPERADOR_EMBARCACION && cantidadDeOperadores() <= 2) {
            throw new BusinessException("La operación en curso debe conservar 2 operadores de embarcación");
        }
        long guias = tripulacion.stream().filter(t -> t.getRol() == Rol.GUIA_TURISTICA).count();
        if (persona.getRol() == Rol.GUIA_TURISTICA && guias <= 1) {
            throw new BusinessException("La operación en curso debe tener al menos un guía");
        }
        tripulacion.removeIf(t -> t.getId().equals(persona.getId()));
    }

    public boolean contieneGuia() {
        return tripulacion.stream().anyMatch(p -> p.getRol() == Rol.GUIA_TURISTICA);
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
        if (!fin.isAfter(ultimoMomentoRegistrado())) {
            throw new BusinessException("La hora de llegada debe ser posterior al zarpe y a la última parada");
        }
        exigirHorometroCompleto();
        if (totalUsuariosTransportados() == 0) {
            throw new BusinessException("La cantidad total de usuarios transportados no puede ser igual a 0. Registra los usuarios para poder finalizar.");
        }
        paradas.add(new Parada(embarcaderoDestino, fin, null));
        this.fechaHoraFin = fin;
        this.horometroFinal = horometroFinal;
        this.estado = EstadoOperacion.FINALIZADA;
    }

    /** §7.1.7: suspensión por clima, nivel del río, autoridad, falla mecánica, etc. (R48: causa tipificada). */
    public void suspender(Instant momento, MotivoSuspension motivo) {
        exigirEnCurso();
        if (motivo == null) {
            throw new BusinessException("Indique el motivo de la suspensión");
        }
        if (!momento.isAfter(fechaHoraInicio)) {
            throw new BusinessException("La suspensión debe ser posterior al zarpe");
        }
        exigirHorometroCompleto();
        this.fechaHoraFin = momento;
        this.motivo = motivo;
        this.motivoSuspension = motivo.texto();
        this.estado = EstadoOperacion.SUSPENDIDA;
    }

    // ---- Horómetro (R37) ----

    /**
     * Lectura de cada motor al zarpar. Si el operador la envía, se usa (y actualiza el motor); si no,
     * se toma la última lectura conocida del motor.
     */
    public void registrarHorometrosIniciales(Map<UUID, BigDecimal> lecturas) {
        Map<UUID, BigDecimal> enviadas = lecturas == null ? Map.of() : lecturas;
        exigirMotoresDeLaEmbarcacion(enviadas.keySet());
        lecturasHorometro.clear();
        for (Motor motor : embarcacion.getMotores()) {
            BigDecimal inicial = enviadas.get(motor.getId());
            if (inicial != null) {
                motor.registrarLectura(inicial);          // no puede ser menor que la última conocida
            }
            lecturasHorometro.add(new LecturaHorometro(motor, motor.getHorometroActual()));
        }
    }

    /**
     * Lectura de cada motor al terminar el viaje (R37: obligatoria para todos los motores); actualiza el
     * horómetro del motor.
     */
    public void registrarHorometrosFinales(Map<UUID, BigDecimal> lecturas) {
        exigirEnCurso();
        Map<UUID, BigDecimal> enviadas = lecturas == null ? Map.of() : lecturas;
        exigirMotoresDeLaEmbarcacion(enviadas.keySet());
        for (LecturaHorometro l : lecturasHorometro) {
            if (!enviadas.containsKey(l.getMotor().getId())) {
                throw new BusinessException("Falta la lectura del horómetro del motor " + l.getMotor().getSerial());
            }
        }
        for (LecturaHorometro l : lecturasHorometro) {
            BigDecimal fin = enviadas.get(l.getMotor().getId());
            l.registrarFinal(fin);
            l.getMotor().registrarLectura(fin);
        }
    }

    /** R37: no se cierra un viaje sin el horómetro inicial y final de cada motor. */
    private void exigirHorometroCompleto() {
        if (lecturasHorometro.isEmpty() && !embarcacion.getMotores().isEmpty()) {
            throw new BusinessException("No se registró el horómetro de los motores al zarpar");
        }
        lecturasHorometro.stream().filter(l -> l.getLecturaFinal() == null).findFirst().ifPresent(l -> {
            throw new BusinessException("Falta la lectura final del horómetro del motor " + l.getMotor().getSerial());
        });
    }

    /** Horas de motor del viaje (suma de los motores); null si falta alguna lectura final. */
    public BigDecimal horasMotor() {
        if (!horometroCompleto()) {
            return null;
        }
        return lecturasHorometro.stream().map(LecturaHorometro::horas).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean horometroCompleto() {
        return !lecturasHorometro.isEmpty() && lecturasHorometro.stream().allMatch(l -> l.getLecturaFinal() != null);
    }

    /** Horas entre el zarpe y la llegada o suspensión; null si el viaje sigue en curso. */
    public BigDecimal horasNavegacion() {
        if (fechaHoraFin == null) {
            return null;
        }
        long minutos = java.time.Duration.between(fechaHoraInicio, fechaHoraFin).toMinutes();
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }

    public List<LecturaHorometro> getLecturasHorometro() {
        return Collections.unmodifiableList(lecturasHorometro);
    }

    private void exigirMotoresDeLaEmbarcacion(Set<UUID> motorIds) {
        Set<UUID> propios = new HashSet<>();
        embarcacion.getMotores().forEach(m -> propios.add(m.getId()));
        for (UUID id : motorIds) {
            if (!propios.contains(id)) {
                throw new BusinessException("El motor " + id + " no pertenece a " + embarcacion.getNombre());
            }
        }
    }

    // ---- Cargue de combustible (§4.1, R16) ----

    /** Abastecimientos hechos antes de este zarpe: quedan en la bitácora del viaje. */
    public void registrarCargues(List<AbastecimientoCombustible> cargues) {
        cargues.forEach(a -> {
            a.asociarA(this);
            abastecimientos.add(a);
        });
        this.cargueCombustible = !abastecimientos.isEmpty();
    }

    public List<AbastecimientoCombustible> getAbastecimientos() {
        return Collections.unmodifiableList(abastecimientos);
    }

    /** Lo llama el constructor de Usuario: mantiene la lista sincronizada en memoria. */
    void agregarUsuario(Usuario usuario) {
        if (estado == EstadoOperacion.SUSPENDIDA) {
            throw new BusinessException("No se pueden registrar usuarios en una operación suspendida");
        }
        usuarios.add(usuario);
    }

    /**
     * Capacidad acumulada (R16): en cada tramo del recorrido, los usuarios a bordo no pueden superar la
     * capacidad de la embarcación. A bordo al zarpar de un embarcadero = abordados en ese embarcadero o
     * antes, menos los que ya bajaron (su destino está en ese embarcadero o antes).
     *
     * @param nuevo registro que se está agregando (ya está en la lista de usuarios: no se cuenta dos veces)
     */
    public void validarCapacidad(Usuario nuevo) {
        int capacidad = embarcacion.getCapacidadPasajeros();
        List<Embarcaderos> ruta = recorrido();
        for (int tramo = 0; tramo < ruta.size() - 1; tramo++) {
            int existentes = 0;
            int nuevos = 0;
            for (Usuario u : usuarios) {
                int aBordo = aBordoAlZarparDe(u, ruta, tramo);
                if (u == nuevo) {
                    nuevos += aBordo;
                } else {
                    existentes += aBordo;
                }
            }
            if (existentes + nuevos > capacidad) {
                Embarcaderos desde = ruta.get(tramo);
                throw new BusinessException("Capacidad excedida al zarpar de " + desde.getDescripcion() + ": van "
                        + existentes + " usuarios a bordo y la capacidad de " + embarcacion.getNombre() + " es "
                        + capacidad + ". Pueden abordar como máximo " + Math.max(0, capacidad - existentes) + " más");
            }
        }
    }

    /** Embarcaderos del recorrido en el orden en que se navegan (origen → destino). */
    public List<Embarcaderos> recorrido() {
        Embarcaderos[] todos = Embarcaderos.values();
        int paso = embarcaderoDestino.ordinal() > embarcaderoOrigen.ordinal() ? 1 : -1;
        List<Embarcaderos> ruta = new ArrayList<>();
        for (int i = embarcaderoOrigen.ordinal(); ; i += paso) {
            ruta.add(todos[i]);
            if (i == embarcaderoDestino.ordinal()) {
                return ruta;
            }
        }
    }

    /** Cuántos usuarios de un registro siguen a bordo al zarpar del embarcadero ruta[tramo]. */
    private static int aBordoAlZarparDe(Usuario u, List<Embarcaderos> ruta, int tramo) {
        if (ruta.indexOf(u.getEmbarcaderoAbordaje()) > tramo) {
            return 0;                                   // todavía no ha abordado
        }
        return u.getPorDestino().entrySet().stream()
                .filter(d -> ruta.indexOf(d.getKey()) > tramo)   // su destino está más adelante
                .mapToInt(Map.Entry::getValue)
                .sum();
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

    /** Salida de la última parada registrada (o el zarpe, si no hay paradas intermedias). */
    private Instant ultimoMomentoRegistrado() {
        return paradas.stream()
                .map(p -> p.getHoraSalida() != null ? p.getHoraSalida() : p.getHoraLlegada())
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(fechaHoraInicio);
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