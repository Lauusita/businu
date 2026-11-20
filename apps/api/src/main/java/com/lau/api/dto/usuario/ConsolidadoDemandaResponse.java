package com.lau.api.dto.usuario;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pasajeros.AgrupacionSerie;
import com.lau.api.dominio.enums.pasajeros.FranjaHoraria;
import com.lau.api.dominio.enums.pasajeros.GrupoEtario;
import com.lau.api.dominio.enums.pasajeros.Sexo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Consolidado de la demanda (§4.2, R22). Fechas y horas en hora de Colombia. Incluye los usuarios de
 * viajes suspendidos (informados también aparte). Cada mapa trae todas las categorías, aunque sean 0.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConsolidadoDemandaResponse(
        LocalDate desde,
        LocalDate hasta,
        Embarcaciones embarcacion,             // filtro aplicado; se omite si no hubo
        Embarcaderos origen,                   // filtro aplicado; se omite si no hubo
        AgrupacionSerie agrupacion,
        int totalUsuarios,
        int viajes,
        BigDecimal promedioPorViaje,           // se omite si no hubo viajes
        Suspendidos suspendidos,
        Map<Sexo, Integer> porSexo,
        Map<GrupoEtario, Integer> porGrupoEtario,
        Map<Embarcaderos, Integer> porOrigen,
        Map<Embarcaderos, Integer> porDestino,
        List<OrigenDestino> origenDestino,
        List<Franja> porFranjaHoraria,
        List<Hora> porHora,
        List<Punto> serie) {

    public record Suspendidos(int viajes, int usuarios) { }

    public record OrigenDestino(Embarcaderos origen, Embarcaderos destino, int usuarios) { }

    /** Mañana, tarde o noche: cuántos usuarios, por sexo y por grupo etario. */
    public record Franja(FranjaHoraria franja, String desde, String hasta, int usuarios,
                         Map<Sexo, Integer> porSexo, Map<GrupoEtario, Integer> porGrupoEtario) { }

    /** Detalle por hora del día (5 = de 05:00 a 05:59). */
    public record Hora(int hora, int usuarios, Map<Sexo, Integer> porSexo, Map<GrupoEtario, Integer> porGrupoEtario) { }

    /** Un punto de la serie: el día, el lunes de la semana o el día 1 del mes. */
    public record Punto(LocalDate fecha, int usuarios, int viajes) { }
}
