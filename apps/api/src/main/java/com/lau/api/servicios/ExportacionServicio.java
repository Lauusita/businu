package com.lau.api.servicios;

import com.lau.api.config.HorarioOperacion;
import com.lau.api.dominio.enums.ConjuntoExportacion;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pasajeros.GrupoEtario;
import com.lau.api.dto.combustible.ConsumoPeriodoResponse;
import com.lau.api.dto.exportacion.Tabla;
import com.lau.api.repositorio.EmbarcacionRepositorio;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Conjuntos de datos para exportar (§4.8) y para el informe mensual (§4.7). Consultas SQL directas,
 * en hora de Colombia, con encabezados legibles. No incluye datos de contacto de las PQRS ni del personal.
 */
@Service
@Transactional(readOnly = true)
public class ExportacionServicio {

    private final NamedParameterJdbcTemplate jdbc;
    private final HorarioOperacion horario;
    private final AbastecimientoServicio abastecimiento;
    private final EmbarcacionRepositorio embarcaciones;

    public ExportacionServicio(NamedParameterJdbcTemplate jdbc, HorarioOperacion horario,
                               AbastecimientoServicio abastecimiento, EmbarcacionRepositorio embarcaciones) {
        this.jdbc = jdbc;
        this.horario = horario;
        this.abastecimiento = abastecimiento;
        this.embarcaciones = embarcaciones;
    }

    public List<Tabla> tablas(ConjuntoExportacion conjunto, LocalDate desde, LocalDate hasta) {
        if (conjunto == ConjuntoExportacion.COMPLETO) {
            return Arrays.stream(ConjuntoExportacion.values())
                    .filter(c -> c != ConjuntoExportacion.COMPLETO)
                    .map(c -> tabla(c, desde, hasta))
                    .toList();
        }
        return List.of(tabla(conjunto, desde, hasta));
    }

    public Tabla tabla(ConjuntoExportacion conjunto, LocalDate desde, LocalDate hasta) {
        return switch (conjunto) {
            case VIAJES -> consulta("Viajes", VIAJES, desde, hasta);
            case PASAJEROS -> consulta("Pasajeros", pasajeros(), desde, hasta);
            case COMBUSTIBLE -> consulta("Combustible", COMBUSTIBLE, desde, hasta);
            case CONSUMO -> consumo(desde, hasta);
            case MANTENIMIENTOS -> consulta("Mantenimientos", MANTENIMIENTOS, desde, hasta);
            case NOVEDADES -> consulta("Novedades", NOVEDADES, desde, hasta);
            case PERSONAL -> consulta("Personal", PERSONAL, desde, hasta);
            case PQRS -> consulta("PQRS", PQRS, desde, hasta);
            case COMPLETO -> throw new IllegalArgumentException("COMPLETO agrupa varias tablas");
        };
    }

    // ---------------------------------------------------------------- Ejecución

    private Tabla consulta(String nombre, String sql, LocalDate desde, LocalDate hasta) {
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("zona", horario.zona().getId())
                .addValue("desde", Timestamp.from(inicio(desde)))
                .addValue("hasta", Timestamp.from(inicio(hasta.plusDays(1))));
        List<String> columnas = new ArrayList<>();
        List<List<Object>> filas = new ArrayList<>();
        jdbc.query(sql, p, rs -> {
            ResultSetMetaData md = rs.getMetaData();
            if (columnas.isEmpty()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    columnas.add(md.getColumnLabel(i));
                }
            }
            List<Object> fila = new ArrayList<>(md.getColumnCount());
            for (int i = 1; i <= md.getColumnCount(); i++) {
                Object v = rs.getObject(i);
                fila.add(v instanceof java.util.UUID || v instanceof java.sql.Date ? v.toString() : v);
            }
            filas.add(fila);
        });
        if (columnas.isEmpty()) {             // sin filas: los encabezados salen del propio SQL
            jdbc.query("SELECT * FROM (" + sql + ") vacio LIMIT 0", p, (org.springframework.jdbc.core.ResultSetExtractor<Void>) rs -> {
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    columnas.add(md.getColumnLabel(i));
                }
                return null;
            });
        }
        return new Tabla(nombre, columnas, filas);
    }

    /** Consumo entre tanques llenos (R37) de cada embarcación, de los periodos que terminan en el rango. */
    private Tabla consumo(LocalDate desde, LocalDate hasta) {
        Instant ini = inicio(desde);
        Instant fin = inicio(hasta.plusDays(1));
        List<List<Object>> filas = new ArrayList<>();
        embarcaciones.findAll().forEach(e -> {
            for (ConsumoPeriodoResponse c : abastecimiento.consumo(e.getId())) {
                if (!c.hasta().isBefore(ini) && c.hasta().isBefore(fin)) {
                    filas.add(Arrays.asList(e.getNombre().name(), fecha(c.desde()), fecha(c.hasta()),
                            c.galonesConsumidos(), c.viajes(), c.horasNavegacion(), c.horasMotor(),
                            c.galonesPorViaje(), c.galonesPorHoraNavegacion(), c.galonesPorHoraMotor()));
                }
            }
        });
        return new Tabla("Consumo", List.of("Embarcación", "Desde (tanque lleno)", "Hasta (tanque lleno)",
                "Galones consumidos", "Viajes", "Horas de navegación", "Horas de motor", "Galones por viaje",
                "Galones por hora de navegación", "Galones por hora de motor"), filas);
    }

    private Instant inicio(LocalDate d) {
        return d.atStartOfDay(horario.zona()).toInstant();
    }

    private String fecha(Instant i) {
        return i.atZone(horario.zona()).toLocalDateTime().toString().replace('T', ' ');
    }

    // ---------------------------------------------------------------- Consultas


    static final String VIAJES = """
            SELECT to_char(o.fecha_hora_inicio AT TIME ZONE :zona, 'YYYY-MM-DD') AS "Fecha",
                   e.nombre AS "Embarcación", o.embarcadero_origen AS "Origen", o.embarcadero_destino AS "Destino",
                   to_char(o.fecha_hora_inicio AT TIME ZONE :zona, 'HH24:MI') AS "Zarpe",
                   to_char(o.fecha_hora_fin AT TIME ZONE :zona, 'HH24:MI') AS "Llegada o suspensión",
                   o.estado AS "Estado",
                   round(CAST(extract(epoch FROM (o.fecha_hora_fin - o.fecha_hora_inicio)) / 3600.0 AS numeric), 2) AS "Horas de navegación",
                   (SELECT sum(h.lectura_final - h.lectura_inicial) FROM operacion_horometros h WHERE h.operacion_id = o.id) AS "Horas de motor",
                   (SELECT coalesce(sum(u.usuarios_transportados), 0) FROM usuarios u WHERE u.operacion_id = o.id) AS "Pasajeros",
                   o.estado_nave AS "Estado de la nave", o.condicion_climatica AS "Clima", o.nivel_rio AS "Nivel del río",
                   (SELECT string_agg(p.nombres || ' ' || p.apellidos || ' (' || p.rol || ')', '; ' ORDER BY p.rol)
                      FROM operacion_tripulacion t JOIN personal p ON p.id = t.personal_id WHERE t.operacion_id = o.id) AS "Tripulación",
                   (SELECT string_agg(c.condicion || ' (' || c.severidad || ')', '; ')
                      FROM operacion_condiciones_mecanicas c WHERE c.operacion_id = o.id) AS "Novedades mecánicas",
                   (SELECT sum(a.galones) FROM abastecimientos_combustible a WHERE a.operacion_id = o.id) AS "Combustible cargado antes del zarpe (gal)",
                   o.novedades AS "Novedades", o.observaciones AS "Observaciones",
                   o.causa_suspension AS "Causa de suspensión", o.detalle_suspension AS "Detalle de suspensión",
                   rp.nombres || ' ' || rp.apellidos AS "Registrado por", o.id AS "Id del viaje"
            FROM operaciones o
            JOIN embarcacion e ON e.id = o.embarcacion_id
            JOIN personal rp ON rp.id = o.registrado_por_id
            WHERE o.fecha_hora_inicio >= :desde AND o.fecha_hora_inicio < :hasta
            ORDER BY o.fecha_hora_inicio
            """;

    /** Una fila por abordaje, con sexo, grupo etario y destino en columnas. */
    static String pasajeros() {
        String grupos = Arrays.stream(GrupoEtario.values())
                .map(g -> "coalesce((SELECT g.cantidad FROM usuarios_grupo_etario g WHERE g.usuario_id = u.id AND g.grupo_etario = '"
                        + g.name() + "'), 0) AS \"" + g.name() + "\"")
                .collect(Collectors.joining(",\n"));
        String destinos = Arrays.stream(Embarcaderos.values())
                .map(d -> "coalesce((SELECT d.cantidad FROM usuarios_destino d WHERE d.usuario_id = u.id AND d.embarcadero_destino = '"
                        + d.name() + "'), 0) AS \"Destino " + d.getDescripcion() + "\"")
                .collect(Collectors.joining(",\n"));
        return """
                SELECT to_char(u.fecha_hora AT TIME ZONE :zona, 'YYYY-MM-DD') AS "Fecha",
                       to_char(u.fecha_hora AT TIME ZONE :zona, 'HH24:MI') AS "Hora",
                       e.nombre AS "Embarcación", o.estado AS "Estado del viaje",
                       u.embarcadero_abordaje AS "Embarcadero de abordaje", u.usuarios_transportados AS "Usuarios",
                       coalesce((SELECT s.cantidad FROM usuarios_sexo s WHERE s.usuario_id = u.id AND s.sexo = 'FEMENINO'), 0) AS "Mujeres",
                       coalesce((SELECT s.cantidad FROM usuarios_sexo s WHERE s.usuario_id = u.id AND s.sexo = 'MASCULINO'), 0) AS "Hombres",
                """ + grupos + ",\n" + destinos + """
                ,
                       rp.nombres || ' ' || rp.apellidos AS "Registrado por", o.id AS "Id del viaje", u.id AS "Id del registro"
                FROM usuarios u
                JOIN operaciones o ON o.id = u.operacion_id
                JOIN embarcacion e ON e.id = o.embarcacion_id
                JOIN personal rp ON rp.id = u.registrado_por_id
                WHERE u.fecha_hora >= :desde AND u.fecha_hora < :hasta
                ORDER BY u.fecha_hora
                """;
    }

    static final String COMBUSTIBLE = """
            SELECT to_char(a.fecha_hora AT TIME ZONE :zona, 'YYYY-MM-DD HH24:MI') AS "Fecha",
                   e.nombre AS "Embarcación", a.tipo_combustible AS "Tipo de combustible", a.galones AS "Galones",
                   a.valor_total AS "Valor total (COP)", round(a.valor_total / a.galones, 2) AS "Precio por galón",
                   a.tanque_lleno AS "Tanque lleno", a.observaciones AS "Observaciones",
                   ad.nombre_original AS "Soporte", ad.sha256 AS "SHA-256 del soporte",
                   a.operacion_id AS "Viaje en cuya bitácora quedó",
                   rp.nombres || ' ' || rp.apellidos AS "Registrado por"
            FROM abastecimientos_combustible a
            JOIN embarcacion e ON e.id = a.embarcacion_id
            JOIN adjuntos ad ON ad.id = a.soporte_id
            JOIN personal rp ON rp.id = a.registrado_por_id
            WHERE a.fecha_hora >= :desde AND a.fecha_hora < :hasta
            ORDER BY a.fecha_hora
            """;

    static final String MANTENIMIENTOS = """
            SELECT to_char(m.fecha_intervencion AT TIME ZONE :zona, 'YYYY-MM-DD') AS "Fecha",
                   e.nombre AS "Embarcación", mo.serial AS "Motor", m.tipo AS "Tipo", m.sistema AS "Sistema",
                   m.descripcion_actividades AS "Actividades", m.ejecutado_por AS "Ejecutado por",
                   re.nombres || ' ' || re.apellidos AS "Responsable",
                   (SELECT string_agg(r.item || ' x ' || r.cantidad || ' ' || r.unidad, '; ')
                      FROM mantenimiento_repuestos r WHERE r.mantenimiento_id = m.id) AS "Repuestos",
                   (SELECT coalesce(sum(r.costo), 0) FROM mantenimiento_repuestos r WHERE r.mantenimiento_id = m.id) AS "Costo de repuestos",
                   m.costo_mano_obra AS "Costo de mano de obra",
                   m.costo_mano_obra + (SELECT coalesce(sum(r.costo), 0) FROM mantenimiento_repuestos r WHERE r.mantenimiento_id = m.id) AS "Costo total",
                   (SELECT string_agg(n.condicion || ' (' || n.severidad || ')', '; ')
                      FROM mantenimiento_novedades n WHERE n.mantenimiento_id = m.id) AS "Novedades",
                   (SELECT string_agg(r.tipo || ': ' || r.descripcion, ' | ')
                      FROM mantenimiento_recomendaciones r WHERE r.mantenimiento_id = m.id) AS "Alertas y recomendaciones",
                   to_char(m.proxima_intervencion AT TIME ZONE :zona, 'YYYY-MM-DD') AS "Próxima intervención",
                   m.condicion_proxima_intervencion AS "Condición de la próxima intervención"
            FROM mantenimientos m
            JOIN embarcacion e ON e.id = m.embarcacion_id
            LEFT JOIN motor mo ON mo.id = m.motor_id
            JOIN personal re ON re.id = m.responsable_id
            WHERE m.fecha_intervencion >= :desde AND m.fecha_intervencion < :hasta
            ORDER BY m.fecha_intervencion
            """;

    /** Novedades operacionales, mecánicas, climáticas y de seguridad (§4.7) de todas las fuentes. */
    static final String NOVEDADES = """
            SELECT to_char(x.f AT TIME ZONE :zona, 'YYYY-MM-DD HH24:MI') AS "Fecha", x.origen AS "Origen",
                   x.lugar AS "Embarcación o lugar", x.categoria AS "Categoría", x.severidad AS "Severidad",
                   x.detalle AS "Detalle", x.estado AS "Estado"
            FROM (
                SELECT o.fecha_hora_inicio AS f, 'Bitácora: novedad mecánica' AS origen, CAST(e.nombre AS varchar) AS lugar,
                       CAST(c.condicion AS varchar) AS categoria, CAST(c.severidad AS varchar) AS severidad,
                       c.detalle AS detalle, CAST(o.estado AS varchar) AS estado
                FROM operacion_condiciones_mecanicas c JOIN operaciones o ON o.id = c.operacion_id
                JOIN embarcacion e ON e.id = o.embarcacion_id
                UNION ALL
                SELECT o.fecha_hora_inicio, 'Bitácora: novedad operacional', e.nombre, 'NOVEDAD', NULL, o.novedades, o.estado
                FROM operaciones o JOIN embarcacion e ON e.id = o.embarcacion_id WHERE o.novedades <> 'Sin novedades'
                UNION ALL
                SELECT o.fecha_hora_inicio, 'Bitácora: clima', e.nombre, o.condicion_climatica, NULL,
                       'Nivel del río: ' || o.nivel_rio, o.estado
                FROM operaciones o JOIN embarcacion e ON e.id = o.embarcacion_id
                WHERE o.condicion_climatica <> 'DESPEJADO' OR o.nivel_rio <> 'NORMAL'
                UNION ALL
                SELECT o.fecha_hora_fin, 'Suspensión de viaje', e.nombre, o.causa_suspension, NULL,
                       coalesce(o.detalle_suspension, o.motivo_suspension), o.estado
                FROM operaciones o JOIN embarcacion e ON e.id = o.embarcacion_id WHERE o.estado = 'SUSPENDIDA'
                UNION ALL
                SELECT s.fecha_inicio, 'Suspensión en muelle', e.nombre, s.causa, NULL,
                       s.detalle || CASE WHEN s.autoridad <> 'No aplica' THEN ' (' || s.autoridad || ')' ELSE '' END,
                       CASE WHEN s.fecha_fin IS NULL THEN 'VIGENTE' ELSE 'LEVANTADA' END
                FROM suspensiones_embarcacion s JOIN embarcacion e ON e.id = s.embarcacion_id
                UNION ALL
                SELECT r.fecha_hora, 'Infraestructura', coalesce(r.embarcadero, a.codigo), r.tipo || ' - ' || r.componente,
                       r.severidad, r.descripcion, r.estado
                FROM registros_infraestructura r LEFT JOIN ayudas_navegacion a ON a.id = r.ayuda_id
            ) x
            WHERE x.f >= :desde AND x.f < :hasta
            ORDER BY x.f
            """;

    /** Relación del personal (§4.7). Sin datos de contacto. No depende del rango de fechas. */
    static final String PERSONAL = """
            SELECT p.tipo_identificacion AS "Tipo de identificación", p.numero_identificacion AS "Número",
                   p.nombres AS "Nombres", p.apellidos AS "Apellidos", p.rol AS "Rol", p.tipo_vinculacion AS "Vinculación",
                   CAST(p.fecha_vinculacion AS varchar) AS "Fecha de vinculación", CAST(p.fecha_retiro AS varchar) AS "Fecha de retiro",
                   CASE WHEN p.contrato_activo THEN 'Activo' ELSE 'Retirado' END AS "Estado",
                   (SELECT string_agg(DISTINCT d.tipo, ', ') FROM documentos_personal d
                     WHERE d.personal_id = p.id AND d.vigente AND d.tipo <> 'PLANILLA_PILA') AS "Documentos vigentes",
                   (SELECT string_agg(to_char(d.periodo, 'YYYY-MM'), ', ' ORDER BY d.periodo) FROM documentos_personal d
                     WHERE d.personal_id = p.id AND d.vigente AND d.tipo = 'PLANILLA_PILA'
                       AND d.periodo >= CAST(:desde AS date) - 31 AND d.periodo < CAST(:hasta AS date)) AS "Planillas PILA del periodo"
            FROM personal p
            WHERE CAST(:desde AS timestamptz) IS NOT NULL
            ORDER BY p.rol, p.apellidos, p.nombres
            """;

    /** PQRS del periodo, sin los datos de contacto del usuario (Ley 1581). */
    static final String PQRS = """
            SELECT q.radicado AS "Radicado", to_char(q.creado_en AT TIME ZONE :zona, 'YYYY-MM-DD HH24:MI') AS "Fecha",
                   q.tipo AS "Tipo", q.embarcadero AS "Embarcadero", q.descripcion AS "Descripción", q.estado AS "Estado",
                   q.respuesta AS "Respuesta", q.entidad_traslado AS "Trasladada a",
                   to_char(q.fecha_respuesta AT TIME ZONE :zona, 'YYYY-MM-DD HH24:MI') AS "Fecha de respuesta",
                   gp.nombres || ' ' || gp.apellidos AS "Gestionada por"
            FROM pqrs q LEFT JOIN personal gp ON gp.id = q.gestionado_por_id
            WHERE q.creado_en >= :desde AND q.creado_en < :hasta
            ORDER BY q.creado_en
            """;
}
