package com.lau.api.dominio.enums.personal;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;

/**
 * Documentos del personal (§4.6 y §7.3.1 del anexo). "Requerido" indica los que debe tener toda persona
 * del rol; los periódicos (planilla PILA) se registran una vez por mes.
 */
@Getter
public enum TipoDocumentoPersonal {
    CEDULA("Documento de identidad", true, false),
    RUT("RUT", true, false),
    HOJA_VIDA_FUNCION_PUBLICA("Hoja de vida (formato de función pública)", true, false),
    CERTIFICADO_ACADEMICO("Certificado de formación académica", true, false),
    CERTIFICADO_EXPERIENCIA("Certificados de experiencia laboral o relacionada", true, false),
    LICENCIA_TRIPULANTE("Licencia o permiso de tripulante", false, false),        // requerida solo a operadores
    CERTIFICADO_COMPETENCIA("Certificaciones o competencias laborales del cargo", false, false),
    AFILIACION_SEGURIDAD_SOCIAL("Afiliación al Sistema de Seguridad Social Integral", true, false),
    PLANILLA_PILA("Planilla de pago de seguridad social (PILA)", false, true),
    OTRO("Otro documento de idoneidad", false, false);

    private final String descripcion;
    private final boolean requeridoParaTodos;
    private final boolean porPeriodo;

    TipoDocumentoPersonal(String descripcion, boolean requeridoParaTodos, boolean porPeriodo) {
        this.descripcion = descripcion;
        this.requeridoParaTodos = requeridoParaTodos;
        this.porPeriodo = porPeriodo;
    }

    /** Documentos que debe tener vigentes una persona del rol (§7.3.1). */
    public static Set<TipoDocumentoPersonal> requeridosPara(Rol rol) {
        Set<TipoDocumentoPersonal> r = EnumSet.noneOf(TipoDocumentoPersonal.class);
        for (TipoDocumentoPersonal t : values()) {
            if (t.requeridoParaTodos) {
                r.add(t);
            }
        }
        if (rol == Rol.OPERADOR_EMBARCACION) {
            r.add(LICENCIA_TRIPULANTE);      // nadie tripula sin licencia o permiso (§7.3.1)
        }
        return r;
    }
}
