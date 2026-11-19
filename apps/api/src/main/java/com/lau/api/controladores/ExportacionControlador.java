package com.lau.api.controladores;

import com.lau.api.config.HorarioOperacion;
import com.lau.api.dominio.enums.ConjuntoExportacion;
import com.lau.api.dto.exportacion.Tabla;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.servicios.ExportacionServicio;
import com.lau.api.utilidades.EscritorTabular;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Exportación de datos para el análisis del Municipio (§4.8). Solo supervisor.
 * GET /exportaciones/{viajes|pasajeros|combustible|consumo|mantenimientos|novedades|personal|pqrs|completo}
 * ?desde=yyyy-MM-dd&hasta=yyyy-MM-dd&formato=xlsx|csv (por defecto: el mes en curso, xlsx).
 */
@RestController
@RequestMapping("/exportaciones")
@PreAuthorize("hasRole('SUPERVISOR')")
public class ExportacionControlador {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExportacionServicio servicio;
    private final EscritorTabular escritor;
    private final HorarioOperacion horario;

    public ExportacionControlador(ExportacionServicio servicio, EscritorTabular escritor, HorarioOperacion horario) {
        this.servicio = servicio;
        this.escritor = escritor;
        this.horario = horario;
    }

    @GetMapping("/{conjunto}")
    public ResponseEntity<byte[]> exportar(
            @PathVariable String conjunto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "xlsx") String formato) {
        ConjuntoExportacion c;
        try {
            c = ConjuntoExportacion.valueOf(conjunto.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Conjunto no válido: " + conjunto);
        }
        LocalDate hoy = LocalDate.now(horario.zona());
        LocalDate ini = desde != null ? desde : hoy.withDayOfMonth(1);
        LocalDate fin = hasta != null ? hasta : hoy;
        if (fin.isBefore(ini)) {
            throw new BusinessException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        boolean csv = "csv".equalsIgnoreCase(formato);
        if (!csv && !"xlsx".equalsIgnoreCase(formato)) {
            throw new BusinessException("Formato no válido: use xlsx o csv");
        }
        if (csv && c == ConjuntoExportacion.COMPLETO) {
            throw new BusinessException("La exportación completa (varias hojas) solo está disponible en xlsx");
        }
        List<Tabla> tablas = servicio.tablas(c, ini, fin);
        String nombre = "businu-" + c.name().toLowerCase(Locale.ROOT) + "-" + ini + "_" + fin + (csv ? ".csv" : ".xlsx");
        return ResponseEntity.ok()
                .contentType(csv ? new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8) : XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(csv ? escritor.csv(tablas.get(0)) : escritor.xlsx(tablas));
    }
}
