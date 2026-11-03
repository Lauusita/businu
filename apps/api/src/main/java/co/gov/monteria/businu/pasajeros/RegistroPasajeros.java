package co.gov.monteria.businu.pasajeros;

import co.gov.monteria.businu.catalogos.Embarcadero;
import co.gov.monteria.businu.compartido.BaseEntity;
import co.gov.monteria.businu.compartido.ReglaNegocioException;
import co.gov.monteria.businu.personal.UsuarioSistema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Registro de usuarios transportados (§4.2) para un origen y un destino. Si en una misma salida
 * hay pasajeros hacia dos destinos, se crean dos registros.
 *
 * <p>Los pasajeros se registran en una cuadrícula grupo etario × sexo; el total se calcula y
 * nunca se digita, así los conteos por edad, por sexo y el total no pueden contradecirse.
 * No se guarda nombre ni documento del pasajero.
 */
@Getter
@Entity
@Table(name = "registro_pasajeros")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistroPasajeros extends BaseEntity {

    /** Momento de captura en el dispositivo. La hora del servidor queda en creado_en. */
    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    /** Viaje de la bitácora (4.1). La llave foránea se agrega cuando exista esa tabla. */
    @Column(name = "viaje_id")
    private UUID viajeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcadero_origen_id", nullable = false)
    private Embarcadero origen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcadero_destino_id", nullable = false)
    private Embarcadero destino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private UsuarioSistema registradoPor;

    @Column(nullable = false)
    private int total;

    @OneToMany(mappedBy = "registro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegistroPasajerosDetalle> detalles = new ArrayList<>();

    /** @param idCliente UUID generado en el dispositivo (modo offline, sincronización idempotente). */
    public RegistroPasajeros(UUID idCliente, Instant fechaHora, Embarcadero origen, Embarcadero destino,
                             UsuarioSistema registradoPor) {
        super(idCliente);
        if (origen.equals(destino)) {
            throw new ReglaNegocioException("El embarcadero de origen y el de destino deben ser distintos");
        }
        this.fechaHora = fechaHora;
        this.origen = origen;
        this.destino = destino;
        this.registradoPor = registradoPor;
    }

    public void asociarViaje(UUID viajeId) {
        this.viajeId = viajeId;
    }

    /** Fija la cantidad de una celda de la cuadrícula. Cantidad 0 elimina la celda. */
    public void establecerCantidad(GrupoEtario grupo, Sexo sexo, int cantidad) {
        if (cantidad < 0) {
            throw new ReglaNegocioException("La cantidad no puede ser negativa");
        }
        detalles.removeIf(d -> d.getGrupoEtario() == grupo && d.getSexo() == sexo);
        if (cantidad > 0) {
            detalles.add(new RegistroPasajerosDetalle(this, grupo, sexo, cantidad));
        }
        total = detalles.stream().mapToInt(RegistroPasajerosDetalle::getCantidad).sum();
    }

    public List<RegistroPasajerosDetalle> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Map<GrupoEtario, Integer> totalPorGrupoEtario() {
        Map<GrupoEtario, Integer> m = new EnumMap<>(GrupoEtario.class);
        detalles.forEach(d -> m.merge(d.getGrupoEtario(), d.getCantidad(), Integer::sum));
        return m;
    }

    public Map<Sexo, Integer> totalPorSexo() {
        Map<Sexo, Integer> m = new EnumMap<>(Sexo.class);
        detalles.forEach(d -> m.merge(d.getSexo(), d.getCantidad(), Integer::sum));
        return m;
    }

    @PrePersist
    @PreUpdate
    void validar() {
        if (total <= 0) {
            throw new ReglaNegocioException("El registro debe tener al menos un pasajero");
        }
    }
}
