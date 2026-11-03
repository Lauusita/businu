package co.gov.monteria.businu.personal;

import co.gov.monteria.businu.compartido.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Matriz de qué documentos exige cada cargo. Alimenta el semáforo de cumplimiento del personal. */
@Getter
@Setter
@Entity
@Table(name = "requisito_documental", uniqueConstraints = @UniqueConstraint(columnNames = {"cargo", "tipo_documento"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequisitoDocumental extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Cargo cargo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 40)
    private TipoDocumentoPersonal tipoDocumento;

    @Column(nullable = false)
    private boolean obligatorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Periodicidad periodicidad;
}
