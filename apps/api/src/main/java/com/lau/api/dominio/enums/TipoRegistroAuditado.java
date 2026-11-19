package com.lau.api.dominio.enums;

import com.lau.api.dominio.*;
import lombok.Getter;

/** Registros cuyo historial de cambios se puede consultar (R5). */
@Getter
public enum TipoRegistroAuditado {
    OPERACION(Operacion.class),
    PASAJEROS(Usuario.class),
    MANTENIMIENTO(Mantenimiento.class),
    ABASTECIMIENTO(AbastecimientoCombustible.class),
    PERSONAL(Personal.class),
    EMBARCACION(Embarcacion.class),
    MOTOR(Motor.class),
    INFRAESTRUCTURA(RegistroInfraestructura.class),
    AYUDA_NAVEGACION(AyudaNavegacion.class),
    SUSPENSION(SuspensionEmbarcacion.class),
    COMUNICACION(ComunicacionFalla.class),
    ADJUNTO(Adjunto.class);

    private final Class<? extends BaseEntity> entidad;

    TipoRegistroAuditado(Class<? extends BaseEntity> entidad) {
        this.entidad = entidad;
    }
}
