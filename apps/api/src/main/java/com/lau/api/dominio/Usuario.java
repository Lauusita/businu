package com.lau.api.dominio;

import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.GrupoEtario;
import com.lau.api.dominio.enums.Sexo;
import com.lau.api.exceptions.BusinessException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Usuarios que abordan en un embarcadero durante una operación (§4.2). Solo se registra al subir:
 * el total del viaje es la suma de los abordajes en todos los embarcaderos del recorrido.
 *
 * <p>Ej.: en Rancho Grande abordan 30 → sexo {FEMENINO: 20, MASCULINO: 10},
 * grupo etario {...}, destino {CENTRO_CALLE_22: 8, CENTRO_VERDE: 22}. Cada distribución suma 30.
 */
@Getter
@Entity
@Table(name = "usuarios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario extends BaseEntity {

    /** Momento de captura en el dispositivo. La hora del servidor queda en creado_en. */
    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operacion_id", nullable = false)
    private Operacion operacion;

    /** Embarcadero donde abordaron (donde está el taquillero). */
    @Enumerated(EnumType.STRING)
    @Column(name = "embarcadero_abordaje", nullable = false, length = 30)
    private Embarcaderos embarcaderoAbordaje;

    @Column(name = "usuarios_transportados", nullable = false)
    private int usuariosTransportados;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuarios_sexo", joinColumns = @JoinColumn(name = "usuario_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "sexo", length = 12)
    @Column(name = "cantidad", nullable = false)
    private Map<Sexo, Integer> porSexo = new EnumMap<>(Sexo.class);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuarios_grupo_etario", joinColumns = @JoinColumn(name = "usuario_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "grupo_etario", length = 20)
    @Column(name = "cantidad", nullable = false)
    private Map<GrupoEtario, Integer> porGrupoEtario = new EnumMap<>(GrupoEtario.class);

    /** Destino declarado al abordar. Ej.: {CENTRO_CALLE_22: 8, CENTRO_VERDE: 22}. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuarios_destino", joinColumns = @JoinColumn(name = "usuario_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "embarcadero_destino", length = 30)
    @Column(name = "cantidad", nullable = false)
    private Map<Embarcaderos, Integer> porDestino = new EnumMap<>(Embarcaderos.class);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Personal registradoPor;

    public Usuario(Instant fechaHora, Operacion operacion, Embarcaderos embarcaderoAbordaje,
                   int usuariosTransportados, Personal registradoPor) {
        if (usuariosTransportados <= 0) {
            throw new BusinessException("Debe haber al menos un usuario");
        }
        int capacidad = operacion.getEmbarcacion().getCapacidadPasajeros();
        if (usuariosTransportados > capacidad) {
            throw new BusinessException("En un abordaje no pueden subir más de " + capacidad + " usuarios");
        }
        if (!operacion.admiteAbordajeEn(embarcaderoAbordaje)) {
            throw new BusinessException("No se puede abordar en " + embarcaderoAbordaje + " en esta operación");
        }
        this.fechaHora = fechaHora;
        this.operacion = operacion;
        this.embarcaderoAbordaje = embarcaderoAbordaje;
        this.usuariosTransportados = usuariosTransportados;
        this.registradoPor = registradoPor;
        operacion.agregarUsuario(this);
    }

    public void registrarSexo(Sexo sexo, int cantidad) {
        actualizar(porSexo, sexo, cantidad);
    }

    public void registrarGrupoEtario(GrupoEtario grupo, int cantidad) {
        actualizar(porGrupoEtario, grupo, cantidad);
    }

    public void registrarDestino(Embarcaderos destino, int cantidad) {
        if (cantidad > 0 && !operacion.esDestinoValido(embarcaderoAbordaje, destino)) {
            throw new BusinessException("Desde " + embarcaderoAbordaje + " no se puede ir a " + destino
                    + " en el sentido de esta operación");
        }
        actualizar(porDestino, destino, cantidad);
    }

    public Map<Sexo, Integer> getPorSexo() {
        return Collections.unmodifiableMap(porSexo);
    }

    public Map<GrupoEtario, Integer> getPorGrupoEtario() {
        return Collections.unmodifiableMap(porGrupoEtario);
    }

    public Map<Embarcaderos, Integer> getPorDestino() {
        return Collections.unmodifiableMap(porDestino);
    }

    @PrePersist
    @PreUpdate
    void validar() {
        exigirSuma("sexo", porSexo);
        exigirSuma("grupo etario", porGrupoEtario);
        exigirSuma("destino", porDestino);
    }

    private static <K> void actualizar(Map<K, Integer> distribucion, K clave, int cantidad) {
        if (cantidad < 0) {
            throw new BusinessException("La cantidad no puede ser negativa");
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
            throw new BusinessException("La distribución por " + nombre + " suma " + suma
                    + " y debe sumar " + usuariosTransportados + " usuarios");
        }
    }
}