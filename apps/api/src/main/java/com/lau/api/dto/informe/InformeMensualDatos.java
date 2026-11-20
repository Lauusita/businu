package com.lau.api.dto.informe;

import com.lau.api.dto.exportacion.Tabla;
import com.lau.api.dto.usuario.ConsolidadoDemandaResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Cifras del informe mensual (§4.7), calculadas de los registros del sistema. Al cerrar el informe se
 * guardan tal cual (JSON) para que lo entregado no cambie aunque después se corrija un dato.
 */
public record InformeMensualDatos(
        YearMonth periodo,
        LocalDate desde,
        LocalDate hasta,
        Instant generadoEn,
        Viajes viajes,
        ConsolidadoDemandaResponse demanda,          // pasajeros y caracterización (R22)
        Combustible combustible,
        List<EstadoEmbarcacion> embarcaciones,
        List<String> alertasMantenimiento,
        Infraestructura infraestructura,
        SeguridadSocial seguridadSocial,
        ResumenPqrs pqrs,
        Map<String, Tabla> tablas) {                 // detalle: bitácoras, mantenimientos, novedades, personal, PQRS…

    public record Viajes(int total, int finalizados, int suspendidos, int enCurso, BigDecimal horasNavegacion,
                         Map<String, Integer> porEmbarcacion, List<Recorrido> porRecorrido) { }

    public record Recorrido(String origen, String destino, int viajes, int pasajeros) { }

    public record Combustible(int abastecimientos, BigDecimal galones, BigDecimal valorTotal,
                              Map<String, BigDecimal> galonesPorEmbarcacion) { }

    public record EstadoEmbarcacion(String nombre, String estado, boolean suspendida, int viajes,
                                    int novedadesMecanicas, int mantenimientos) { }

    public record Infraestructura(int registrosDelMes, Map<String, Integer> porTipo, int abiertos,
                                  int pendientesDeComunicar) { }

    public record SeguridadSocial(int personalActivo, int conPlanilla, List<String> sinPlanilla) { }

    public record ResumenPqrs(int total, Map<String, Integer> porTipo, Map<String, Integer> porEstado) { }
}
