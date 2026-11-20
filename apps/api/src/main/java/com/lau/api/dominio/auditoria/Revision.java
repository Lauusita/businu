package com.lau.api.dominio.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Revisión de Envers (R5): cada transacción que crea, modifica o elimina registros auditados genera una
 * revisión con la fecha y el autor. Las tablas *_aud guardan el estado de cada registro en cada revisión.
 */
@Getter
@Setter
@Entity
@Table(name = "revisiones")
@RevisionEntity(AutorRevisionListener.class)
public class Revision {

    @Id
    @GeneratedValue
    @RevisionNumber
    private Long id;

    /** Milisegundos desde 1970 (UTC). */
    @RevisionTimestamp
    @Column(nullable = false)
    private long fecha;

    /** Personal autenticado que hizo el cambio; null si lo hizo el sistema (datos de prueba, tareas internas). */
    @Column(name = "personal_id")
    private UUID personalId;
}
