package co.gov.monteria.businu.catalogos;

import co.gov.monteria.businu.compartido.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Embarcaderos (Rancho Grande, Centro – Calle 22, Centro Verde) y estaciones futuras. Es tabla, no enum, para poder agregar la estación de Unicórdoba sin desplegar. */
@Getter
@Setter
@Entity
@Table(name = "embarcadero")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Embarcadero extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEmbarcadero tipo;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    /** Posición en el recorrido, de sur a norte. */
    @Column(name = "orden_ruta", nullable = false)
    private int ordenRuta;

    @Column(nullable = false)
    private boolean activo;

    public Embarcadero(String codigo, String nombre, TipoEmbarcadero tipo, int ordenRuta) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ordenRuta = ordenRuta;
        this.activo = true;
    }
}
