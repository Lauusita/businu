package com.lau.api.dominio;

import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.GrupoEtario;
import com.lau.api.dominio.enums.Sexo;
import com.lau.api.exceptions.ReglaNegocioException;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Usuarios transportados en una operación (§4.2 del anexo): cuántas personas viajaron de un
 * embarcadero a otro en una embarcación, y cómo se reparten por sexo y por grupo etario.
 *
 * <p>Ejemplo: 30 usuarios → sexo {MUJER: 20, HOMBRE: 10} y grupo etario
 * {PRIMERA_INFANCIA: 2, JUVENTUD: 12, ADULTEZ: 16}. Cada distribución debe sumar el total.
 *
 */
@Getter
@Entity
@Table(name = "usuarios")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Usuario extends BaseEntity {

    /** Momento de captura en el dispositivo. La hora del servidor queda en creado_en. */
    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    /** Viaje de la bitácora (4.1). La llave foránea se agrega cuando exista esa tabla. */
    @Column(name = "viaje_id")
    private UUID viajeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "embarcacion_id", nullable = false)
    private Embarcacion embarcacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "embarcadero_origen", nullable = false)
    private Embarcaderos embarcaderoOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "embarcadero_destino", nullable = false)
    private Embarcaderos embarcaderoDestino;

    @Column(name = "usuarios_transportados", nullable = false)
    private int usuariosTransportados;

    /** Ej.: {MUJER: 20, HOMBRE: 10}. Tabla usuarios_sexo. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuarios_sexo", joinColumns = @JoinColumn(name = "usuario_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "sexo", length = 12)
    @Column(name = "cantidad", nullable = false)
    private Map<Sexo, Integer> porSexo = new EnumMap<>(Sexo.class);

    /** Ej.: {PRIMERA_INFANCIA: 2, ADULTEZ: 16, ...}. Tabla usuarios_grupo_etario. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuarios_grupo_etario", joinColumns = @JoinColumn(name = "usuario_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "grupo_etario", length = 20)
    @Column(name = "cantidad", nullable = false)
    private Map<GrupoEtario, Integer> porGrupoEtario = new EnumMap<>(GrupoEtario.class);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    public Usuario(Instant fechaHora, Embarcacion embarcacion, Embarcaderos embarcaderoOrigen,
                   Embarcaderos embarcaderoDestino, int usuariosTransportados, Personal registradoPor) {

        if (embarcaderoOrigen.equals(embarcaderoDestino)) {
            throw new ReglaNegocioException("El embarcadero de origen y el de destino deben ser distintos");
        }
        if (usuariosTransportados <= 0) {
            throw new ReglaNegocioException("Debe haber al menos un usuario transportado");
        }
        if (usuariosTransportados > embarcacion.getCapacidadPasajeros()) {
            throw new ReglaNegocioException("Los usuarios transportados (" + usuariosTransportados
                    + ") superan la capacidad de la embarcación (" + embarcacion.getCapacidadPasajeros() + ")");
        }

        this.fechaHora = fechaHora;
        this.embarcacion = embarcacion;
        this.embarcaderoOrigen = embarcaderoOrigen;
        this.embarcaderoDestino = embarcaderoDestino;
        this.usuariosTransportados = usuariosTransportados;
        this.registradoPor = registradoPor;
    }

    public void asociarViaje(UUID viajeId) {
        this.viajeId = viajeId;
    }

    public void registrarSexo(Sexo sexo, int cantidad) {
        actualizar(porSexo, sexo, cantidad);
    }

    public void registrarGrupoEtario(GrupoEtario grupo, int cantidad) {
        actualizar(porGrupoEtario, grupo, cantidad);
    }

    public Map<Sexo, Integer> getPorSexo() {
        return Collections.unmodifiableMap(porSexo);
    }

    public Map<GrupoEtario, Integer> getPorGrupoEtario() {
        return Collections.unmodifiableMap(porGrupoEtario);
    }

    @PrePersist
    @PreUpdate
    void validar() {
        exigirSuma("sexo", porSexo);
        exigirSuma("grupo etario", porGrupoEtario);
    }

    private static <K> void actualizar(Map<K, Integer> distribucion, K clave, int cantidad) {
        if (cantidad < 0) {
            throw new ReglaNegocioException("La cantidad no puede ser negativa");
        }
        if (cantidad == 0) {
            distribucion.remove(clave);
        } else {
            distribucion.put(clave, cantidad);
        }
    }

    private void exigirSuma(String nombre, Map<?, Integer> distribucion) {
        int suma = distribucion.values().stream().mapToInt(Integer::intValue).sum();
        if (suma != usuariosTransportados) {
            throw new ReglaNegocioException("La distribución por " + nombre + " suma " + suma
                    + " y debe sumar " + usuariosTransportados + " usuarios transportados");
        }
    }
}