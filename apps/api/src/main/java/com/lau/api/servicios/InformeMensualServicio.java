package com.lau.api.servicios;

import com.lau.api.config.HorarioOperacion;
import com.lau.api.dominio.InformeMensual;
import com.lau.api.dominio.Personal;
import com.lau.api.dominio.enums.ConjuntoExportacion;
import com.lau.api.dominio.enums.EstadoInforme;
import com.lau.api.dominio.enums.pasajeros.AgrupacionSerie;
import com.lau.api.dto.exportacion.Tabla;
import com.lau.api.dto.informe.InformeMensualDatos;
import com.lau.api.dto.informe.InformeMensualDatos.*;
import com.lau.api.dto.informe.InformeMensualResponse;
import com.lau.api.dto.informe.RedactarInformeRequest;
import com.lau.api.exceptions.BusinessException;
import com.lau.api.exceptions.NotFoundException;
import com.lau.api.repositorio.InformeMensualRepositorio;
import com.lau.api.repositorio.PersonalRepositorio;
import com.lau.api.utilidades.Formato;
import com.lau.api.utilidades.GeneradorPdf;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** Informe mensual de operación (§4.7). */
@Service
@Transactional(readOnly = true)
public class InformeMensualServicio {

    private final InformeMensualRepositorio informes;
    private final PersonalRepositorio personal;
    private final ExportacionServicio exportacion;
    private final ConsolidadoDemandaServicio demanda;
    private final NamedParameterJdbcTemplate jdbc;
    private final HorarioOperacion horario;
    private final GeneradorPdf pdf;
    private final JsonMapper json;

    public InformeMensualServicio(InformeMensualRepositorio informes, PersonalRepositorio personal,
                                  ExportacionServicio exportacion, ConsolidadoDemandaServicio demanda,
                                  NamedParameterJdbcTemplate jdbc, HorarioOperacion horario, GeneradorPdf pdf,
                                  JsonMapper json) {
        this.informes = informes;
        this.personal = personal;
        this.exportacion = exportacion;
        this.demanda = demanda;
        this.jdbc = jdbc;
        this.horario = horario;
        this.pdf = pdf;
        this.json = json;
    }

    // ---------------------------------------------------------------- Consulta y redacción

    public InformeMensualResponse obtener(YearMonth mes) {
        return informes.findByPeriodo(mes.atDay(1))
                .map(i -> toResponse(i, datosDe(i)))
                .orElseGet(() -> new InformeMensualResponse(mes, EstadoInforme.BORRADOR, "", "", "", "",
                        null, null, null, null, null, calcular(mes)));
    }

    @Transactional
    public InformeMensualResponse redactar(YearMonth mes, RedactarInformeRequest r) {
        InformeMensual i = informes.findByPeriodo(mes.atDay(1)).orElseGet(() -> informes.save(new InformeMensual(mes)));
        i.redactar(r.resumenActividades(), r.conclusiones(), r.recomendaciones(), r.otrosDocumentos());
        return toResponse(i, calcular(mes));
    }

    /** Congela las cifras. Solo cuando el mes ya terminó (hora de Colombia). */
    @Transactional
    public InformeMensualResponse cerrar(YearMonth mes, UUID usuarioId) {
        if (!YearMonth.now(horario.zona()).isAfter(mes)) {
            throw new BusinessException("El informe de " + mes + " se puede cerrar cuando termine el mes");
        }
        InformeMensual i = buscar(mes);
        InformeMensualDatos datos = calcular(mes);
        String texto = json.writeValueAsString(datos);
        i.cerrar(texto, sha256(texto), Instant.now(), buscarPersonal(usuarioId));
        return toResponse(i, datos);
    }

    @Transactional
    public InformeMensualResponse entregar(YearMonth mes, UUID usuarioId) {
        InformeMensual i = buscar(mes);
        i.entregar(Instant.now(), buscarPersonal(usuarioId));
        return toResponse(i, datosDe(i));
    }

    /** PDF: cifras congeladas si está cerrado; en vivo y marcado como BORRADOR si no. */
    public byte[] pdf(YearMonth mes) throws IOException {
        InformeMensualResponse r = obtener(mes);
        Map<String, Object> v = new HashMap<>();
        v.put("informe", r);
        v.put("d", r.datos());
        v.put("fmt", new Formato());
        v.put("bitacoras", columnas(r.datos().tablas().get("VIAJES"), "Fecha", "Embarcación", "Origen", "Destino",
                "Zarpe", "Llegada o suspensión", "Estado", "Pasajeros", "Horas de navegación", "Clima"));
        v.put("mantenimientos", columnas(r.datos().tablas().get("MANTENIMIENTOS"), "Fecha", "Embarcación", "Motor",
                "Tipo", "Sistema", "Actividades", "Costo total", "Próxima intervención"));
        v.put("novedades", r.datos().tablas().get("NOVEDADES"));
        v.put("personal", columnas(r.datos().tablas().get("PERSONAL"), "Número", "Nombres", "Apellidos", "Rol",
                "Vinculación", "Estado", "Planillas PILA del periodo"));
        v.put("pqrs", columnas(r.datos().tablas().get("PQRS"), "Radicado", "Fecha", "Tipo", "Descripción", "Estado",
                "Respuesta"));
        v.put("consumo", r.datos().tablas().get("CONSUMO"));
        return pdf.generar("informe-mensual", v);
    }

    // ---------------------------------------------------------------- Cálculo de las cifras

    public InformeMensualDatos calcular(YearMonth mes) {
        LocalDate desde = mes.atDay(1);
        LocalDate hasta = mes.atEndOfMonth();
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("desde", Timestamp.from(desde.atStartOfDay(horario.zona()).toInstant()))
                .addValue("hasta", Timestamp.from(hasta.plusDays(1).atStartOfDay(horario.zona()).toInstant()))
                .addValue("periodo", desde);

        Map<String, Tabla> tablas = new LinkedHashMap<>();
        for (ConjuntoExportacion c : List.of(ConjuntoExportacion.VIAJES, ConjuntoExportacion.MANTENIMIENTOS,
                ConjuntoExportacion.NOVEDADES, ConjuntoExportacion.PERSONAL, ConjuntoExportacion.PQRS,
                ConjuntoExportacion.COMBUSTIBLE, ConjuntoExportacion.CONSUMO)) {
            tablas.put(c.name(), exportacion.tabla(c, desde, hasta));
        }

        return new InformeMensualDatos(mes, desde, hasta, Instant.now(), viajes(p),
                demanda.consolidar(desde, hasta, null, null, AgrupacionSerie.SEMANA),
                combustible(p), embarcaciones(p), alertas(p), infraestructura(p), seguridadSocial(p), pqrs(p), tablas);
    }

    private Viajes viajes(MapSqlParameterSource p) {
        String base = " FROM operaciones o JOIN embarcacion e ON e.id = o.embarcacion_id "
                + "WHERE o.fecha_hora_inicio >= :desde AND o.fecha_hora_inicio < :hasta ";
        Map<String, Object> t = jdbc.queryForMap("SELECT count(*) AS total, "
                + "count(*) FILTER (WHERE o.estado = 'FINALIZADA') AS fin, count(*) FILTER (WHERE o.estado = 'SUSPENDIDA') AS susp, "
                + "count(*) FILTER (WHERE o.estado = 'EN_CURSO') AS curso, "
                + "coalesce(round(CAST(sum(extract(epoch FROM (o.fecha_hora_fin - o.fecha_hora_inicio))) / 3600.0 AS numeric), 2), 0) AS horas"
                + base, p);
        Map<String, Integer> porEmbarcacion = new LinkedHashMap<>();
        jdbc.query("SELECT e.nombre AS n, count(*) AS c" + base + "GROUP BY 1 ORDER BY 1", p,
                rs -> { porEmbarcacion.put(rs.getString("n"), rs.getInt("c")); });
        List<Recorrido> recorridos = jdbc.query("SELECT o.embarcadero_origen AS o, o.embarcadero_destino AS d, count(*) AS v, "
                + "coalesce(sum((SELECT sum(u.usuarios_transportados) FROM usuarios u WHERE u.operacion_id = o.id)), 0) AS pax"
                + base + "GROUP BY 1, 2 ORDER BY 3 DESC", p,
                (rs, i) -> new Recorrido(rs.getString("o"), rs.getString("d"), rs.getInt("v"), rs.getInt("pax")));
        return new Viajes(entero(t.get("total")), entero(t.get("fin")), entero(t.get("susp")), entero(t.get("curso")),
                (BigDecimal) t.get("horas"), porEmbarcacion, recorridos);
    }

    private Combustible combustible(MapSqlParameterSource p) {
        String base = " FROM abastecimientos_combustible a JOIN embarcacion e ON e.id = a.embarcacion_id "
                + "WHERE a.fecha_hora >= :desde AND a.fecha_hora < :hasta ";
        Map<String, Object> t = jdbc.queryForMap("SELECT count(*) AS n, coalesce(sum(a.galones), 0) AS g, "
                + "coalesce(sum(a.valor_total), 0) AS v" + base, p);
        Map<String, BigDecimal> porEmbarcacion = new LinkedHashMap<>();
        jdbc.query("SELECT e.nombre AS n, sum(a.galones) AS g" + base + "GROUP BY 1 ORDER BY 1", p,
                rs -> { porEmbarcacion.put(rs.getString("n"), rs.getBigDecimal("g")); });
        return new Combustible(entero(t.get("n")), (BigDecimal) t.get("g"), (BigDecimal) t.get("v"), porEmbarcacion);
    }

    private List<EstadoEmbarcacion> embarcaciones(MapSqlParameterSource p) {
        return jdbc.query("""
                SELECT e.nombre AS n, e.estado AS est,
                       EXISTS (SELECT 1 FROM suspensiones_embarcacion s WHERE s.embarcacion_id = e.id AND s.fecha_fin IS NULL) AS susp,
                       (SELECT count(*) FROM operaciones o WHERE o.embarcacion_id = e.id
                          AND o.fecha_hora_inicio >= :desde AND o.fecha_hora_inicio < :hasta) AS viajes,
                       (SELECT count(*) FROM operacion_condiciones_mecanicas c JOIN operaciones o ON o.id = c.operacion_id
                         WHERE o.embarcacion_id = e.id AND o.fecha_hora_inicio >= :desde AND o.fecha_hora_inicio < :hasta) AS nov,
                       (SELECT count(*) FROM mantenimientos m WHERE m.embarcacion_id = e.id
                          AND m.fecha_intervencion >= :desde AND m.fecha_intervencion < :hasta) AS mant
                FROM embarcacion e ORDER BY e.nombre
                """, p, (rs, i) -> new EstadoEmbarcacion(rs.getString("n"), rs.getString("est"), rs.getBoolean("susp"),
                rs.getInt("viajes"), rs.getInt("nov"), rs.getInt("mant")));
    }

    /** Recomendaciones de los mantenimientos del mes, novedades graves y próximas intervenciones. */
    private List<String> alertas(MapSqlParameterSource p) {
        return jdbc.queryForList("""
                SELECT txt FROM (
                  SELECT m.fecha_intervencion AS f, e.nombre || ' · ' || r.tipo || ': ' || r.descripcion AS txt
                  FROM mantenimiento_recomendaciones r JOIN mantenimientos m ON m.id = r.mantenimiento_id
                  JOIN embarcacion e ON e.id = m.embarcacion_id
                  WHERE m.fecha_intervencion >= :desde AND m.fecha_intervencion < :hasta
                  UNION ALL
                  SELECT o.fecha_hora_inicio, e.nombre || ' · novedad ' || c.severidad || ' en viaje: ' || c.condicion
                         || CASE WHEN c.detalle <> 'Sin detalle' THEN ' (' || c.detalle || ')' ELSE '' END
                  FROM operacion_condiciones_mecanicas c JOIN operaciones o ON o.id = c.operacion_id
                  JOIN embarcacion e ON e.id = o.embarcacion_id
                  WHERE c.severidad IN ('ALTA', 'CRITICA') AND o.fecha_hora_inicio >= :desde AND o.fecha_hora_inicio < :hasta
                  UNION ALL
                  SELECT m.proxima_intervencion, e.nombre || ' · próxima intervención programada: '
                         || to_char(m.proxima_intervencion AT TIME ZONE 'America/Bogota', 'YYYY-MM-DD')
                         || ' (' || m.condicion_proxima_intervencion || ')'
                  FROM mantenimientos m JOIN embarcacion e ON e.id = m.embarcacion_id
                  WHERE m.proxima_intervencion >= :hasta AND m.proxima_intervencion < CAST(:hasta AS timestamptz) + interval '45 days'
                ) x ORDER BY f
                """, p, String.class);
    }

    private Infraestructura infraestructura(MapSqlParameterSource p) {
        Map<String, Integer> porTipo = new LinkedHashMap<>();
        jdbc.query("SELECT r.tipo AS t, count(*) AS c FROM registros_infraestructura r "
                + "WHERE r.fecha_hora >= :desde AND r.fecha_hora < :hasta GROUP BY 1 ORDER BY 1", p,
                rs -> { porTipo.put(rs.getString("t"), rs.getInt("c")); });
        Map<String, Object> t = jdbc.queryForMap("SELECT count(*) FILTER (WHERE estado <> 'CERRADO') AS abiertos, "
                + "count(*) FILTER (WHERE estado = 'ABIERTO') AS pend FROM registros_infraestructura", p);
        return new Infraestructura(porTipo.values().stream().mapToInt(Integer::intValue).sum(), porTipo,
                entero(t.get("abiertos")), entero(t.get("pend")));
    }

    /** Planillas PILA del mes (§4.7: soportes de seguridad social) del personal con contrato activo. */
    private SeguridadSocial seguridadSocial(MapSqlParameterSource p) {
        List<Map<String, Object>> filas = jdbc.queryForList("""
                SELECT p.nombres || ' ' || p.apellidos || ' (' || p.rol || ')' AS nombre,
                       EXISTS (SELECT 1 FROM documentos_personal d WHERE d.personal_id = p.id AND d.vigente
                               AND d.tipo = 'PLANILLA_PILA' AND d.periodo = :periodo) AS tiene
                FROM personal p WHERE p.contrato_activo ORDER BY p.rol, p.apellidos
                """, p);
        List<String> sin = filas.stream().filter(f -> !(Boolean) f.get("tiene")).map(f -> (String) f.get("nombre")).toList();
        return new SeguridadSocial(filas.size(), filas.size() - sin.size(), sin);
    }

    private ResumenPqrs pqrs(MapSqlParameterSource p) {
        Map<String, Integer> porTipo = new LinkedHashMap<>();
        Map<String, Integer> porEstado = new LinkedHashMap<>();
        String base = " FROM pqrs q WHERE q.creado_en >= :desde AND q.creado_en < :hasta GROUP BY 1 ORDER BY 1";
        jdbc.query("SELECT q.tipo AS k, count(*) AS c" + base, p, rs -> { porTipo.put(rs.getString("k"), rs.getInt("c")); });
        jdbc.query("SELECT q.estado AS k, count(*) AS c" + base, p, rs -> { porEstado.put(rs.getString("k"), rs.getInt("c")); });
        return new ResumenPqrs(porTipo.values().stream().mapToInt(Integer::intValue).sum(), porTipo, porEstado);
    }

    // ---------------------------------------------------------------- Auxiliares

    private InformeMensualDatos datosDe(InformeMensual i) {
        return i.getDatosCongelados() != null
                ? json.readValue(i.getDatosCongelados(), InformeMensualDatos.class)
                : calcular(i.mes());
    }

    private static InformeMensualResponse toResponse(InformeMensual i, InformeMensualDatos datos) {
        return new InformeMensualResponse(i.mes(), i.getEstado(), i.getResumenActividades(), i.getConclusiones(),
                i.getRecomendaciones(), i.getOtrosDocumentos(), i.getFechaCierre(),
                i.getCerradoPor() == null ? null : i.getCerradoPor().nombreCompleto(), i.getFechaEntrega(),
                i.getEntregadoPor() == null ? null : i.getEntregadoPor().nombreCompleto(), i.getHuellaDatos(), datos);
    }

    /** Proyección de columnas para que las tablas quepan en una página del PDF. */
    static Tabla columnas(Tabla t, String... nombres) {
        if (t == null) {
            return null;
        }
        List<Integer> indices = new ArrayList<>();
        for (String n : nombres) {
            int i = t.columnas().indexOf(n);
            if (i >= 0) {
                indices.add(i);
            }
        }
        List<List<Object>> filas = t.filas().stream()
                .map(f -> indices.stream().map(f::get).toList())
                .toList();
        return new Tabla(t.nombre(), indices.stream().map(t.columnas()::get).toList(), filas);
    }

    private InformeMensual buscar(YearMonth mes) {
        return informes.findByPeriodo(mes.atDay(1)).orElseThrow(() ->
                new NotFoundException("Aún no se ha redactado el informe de " + mes + " (PUT /informes/mensual/" + mes + ")"));
    }

    private Personal buscarPersonal(UUID id) {
        return personal.findById(id).orElseThrow(() -> new NotFoundException("Personal no encontrado"));
    }

    private static int entero(Object v) {
        return v == null ? 0 : ((Number) v).intValue();
    }

    private static String sha256(String texto) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
