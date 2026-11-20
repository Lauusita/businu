package com.lau.api.servicios;

import com.lau.api.config.HorarioOperacion;
import com.lau.api.dominio.enums.operacion.Embarcaciones;
import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pasajeros.AgrupacionSerie;
import com.lau.api.dominio.enums.pasajeros.FranjaHoraria;
import com.lau.api.dominio.enums.pasajeros.GrupoEtario;
import com.lau.api.dominio.enums.pasajeros.Sexo;
import com.lau.api.dto.usuario.ConsolidadoDemandaResponse;
import com.lau.api.dto.usuario.ConsolidadoDemandaResponse.*;
import com.lau.api.exceptions.BusinessException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consolidado de la demanda (§4.2, R22). Las sumas se hacen en PostgreSQL sobre los abordajes y sus
 * distribuciones (sexo, grupo etario, destino), en hora de Colombia.
 *
 * <p>Sexo, grupo etario y destino se guardan como distribuciones separadas de cada abordaje, así que se
 * suman por separado (y por franja u hora), pero no se cruzan entre sí.
 */
@Service
@Transactional(readOnly = true)
public class ConsolidadoDemandaServicio {

    /** Abordajes filtrados: base de todas las consultas. */
    private static final String BASE = """
            WITH base AS (
                SELECT u.id, u.usuarios_transportados AS n, u.embarcadero_abordaje AS origen,
                       u.operacion_id, o.estado,
                       (u.fecha_hora AT TIME ZONE :zona) AS local
                FROM usuarios u
                JOIN operaciones o ON o.id = u.operacion_id
                JOIN embarcacion e ON e.id = o.embarcacion_id
                WHERE u.fecha_hora >= :desde AND u.fecha_hora < :hasta
                  AND (CAST(:embarcacion AS varchar) IS NULL OR e.nombre = :embarcacion)
                  AND (CAST(:origen AS varchar) IS NULL OR u.embarcadero_abordaje = :origen)
            )
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final HorarioOperacion horario;

    public ConsolidadoDemandaServicio(NamedParameterJdbcTemplate jdbc, HorarioOperacion horario) {
        this.jdbc = jdbc;
        this.horario = horario;
    }

    public ConsolidadoDemandaResponse consolidar(LocalDate desde, LocalDate hasta, Embarcaciones embarcacion,
                                                 Embarcaderos origen, AgrupacionSerie agrupacion) {
        LocalDate hoy = LocalDate.now(horario.zona());
        LocalDate ini = desde != null ? desde : hoy.withDayOfMonth(1);      // por defecto: el mes en curso
        LocalDate fin = hasta != null ? hasta : hoy;
        if (fin.isBefore(ini)) {
            throw new BusinessException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        AgrupacionSerie unidad = agrupacion != null ? agrupacion : AgrupacionSerie.DIA;

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("zona", horario.zona().getId())
                .addValue("desde", Timestamp.from(ini.atStartOfDay(horario.zona()).toInstant()))
                .addValue("hasta", Timestamp.from(fin.plusDays(1).atStartOfDay(horario.zona()).toInstant()))
                .addValue("embarcacion", embarcacion == null ? null : embarcacion.name(), Types.VARCHAR)
                .addValue("origen", origen == null ? null : origen.name(), Types.VARCHAR)
                .addValue("unidad", unidad.sql());

        // ---- Totales (incluye suspendidos, que además se informan aparte)
        Map<String, Object> t = jdbc.queryForMap(BASE + """
                SELECT COALESCE(SUM(n), 0) AS usuarios, COUNT(DISTINCT operacion_id) AS viajes,
                       COALESCE(SUM(n) FILTER (WHERE estado = 'SUSPENDIDA'), 0) AS usuarios_susp,
                       COUNT(DISTINCT operacion_id) FILTER (WHERE estado = 'SUSPENDIDA') AS viajes_susp
                FROM base
                """, p);
        int total = entero(t.get("usuarios"));
        int viajes = entero(t.get("viajes"));

        // ---- Por hora: total, sexo y grupo etario (las franjas se arman a partir de las horas)
        Map<Integer, Integer> usuariosPorHora = new TreeMap<>();
        jdbc.query(BASE + "SELECT EXTRACT(HOUR FROM local)::int AS h, SUM(n) AS c FROM base GROUP BY 1", p,
                rs -> { usuariosPorHora.put(rs.getInt("h"), rs.getInt("c")); });
        Map<Integer, Map<Sexo, Integer>> sexoPorHora = new TreeMap<>();
        jdbc.query(BASE + """
                SELECT EXTRACT(HOUR FROM b.local)::int AS h, s.sexo AS k, SUM(s.cantidad) AS c
                FROM base b JOIN usuarios_sexo s ON s.usuario_id = b.id GROUP BY 1, 2
                """, p, rs -> { sexoPorHora.computeIfAbsent(rs.getInt("h"), h -> vacio(Sexo.class))
                        .merge(Sexo.valueOf(rs.getString("k")), rs.getInt("c"), Integer::sum); });
        Map<Integer, Map<GrupoEtario, Integer>> grupoPorHora = new TreeMap<>();
        jdbc.query(BASE + """
                SELECT EXTRACT(HOUR FROM b.local)::int AS h, g.grupo_etario AS k, SUM(g.cantidad) AS c
                FROM base b JOIN usuarios_grupo_etario g ON g.usuario_id = b.id GROUP BY 1, 2
                """, p, rs -> { grupoPorHora.computeIfAbsent(rs.getInt("h"), h -> vacio(GrupoEtario.class))
                        .merge(GrupoEtario.valueOf(rs.getString("k")), rs.getInt("c"), Integer::sum); });

        List<Hora> porHora = new ArrayList<>();
        Map<FranjaHoraria, int[]> usuariosFranja = new EnumMap<>(FranjaHoraria.class);
        Map<FranjaHoraria, Map<Sexo, Integer>> sexoFranja = new EnumMap<>(FranjaHoraria.class);
        Map<FranjaHoraria, Map<GrupoEtario, Integer>> grupoFranja = new EnumMap<>(FranjaHoraria.class);
        for (FranjaHoraria f : FranjaHoraria.values()) {
            usuariosFranja.put(f, new int[1]);
            sexoFranja.put(f, vacio(Sexo.class));
            grupoFranja.put(f, vacio(GrupoEtario.class));
        }
        Map<Sexo, Integer> porSexo = vacio(Sexo.class);
        Map<GrupoEtario, Integer> porGrupo = vacio(GrupoEtario.class);
        usuariosPorHora.forEach((h, c) -> {
            Map<Sexo, Integer> s = sexoPorHora.getOrDefault(h, vacio(Sexo.class));
            Map<GrupoEtario, Integer> g = grupoPorHora.getOrDefault(h, vacio(GrupoEtario.class));
            porHora.add(new Hora(h, c, s, g));
            FranjaHoraria f = FranjaHoraria.de(h);
            usuariosFranja.get(f)[0] += c;
            s.forEach((k, v) -> { sexoFranja.get(f).merge(k, v, Integer::sum); porSexo.merge(k, v, Integer::sum); });
            g.forEach((k, v) -> { grupoFranja.get(f).merge(k, v, Integer::sum); porGrupo.merge(k, v, Integer::sum); });
        });
        List<Franja> porFranja = Arrays.stream(FranjaHoraria.values())
                .map(f -> new Franja(f, f.getDesde(), f.getHasta(), usuariosFranja.get(f)[0], sexoFranja.get(f), grupoFranja.get(f)))
                .toList();

        // ---- Origen y destino
        Map<Embarcaderos, Integer> porOrigen = vacio(Embarcaderos.class);
        Map<Embarcaderos, Integer> porDestino = vacio(Embarcaderos.class);
        List<OrigenDestino> od = new ArrayList<>();
        jdbc.query(BASE + """
                SELECT b.origen AS o, d.embarcadero_destino AS d, SUM(d.cantidad) AS c
                FROM base b JOIN usuarios_destino d ON d.usuario_id = b.id GROUP BY 1, 2 ORDER BY 1, 2
                """, p, rs -> {
                    Embarcaderos o = Embarcaderos.valueOf(rs.getString("o"));
                    Embarcaderos d = Embarcaderos.valueOf(rs.getString("d"));
                    int c = rs.getInt("c");
                    od.add(new OrigenDestino(o, d, c));
                    porOrigen.merge(o, c, Integer::sum);
                    porDestino.merge(d, c, Integer::sum);
                });

        // ---- Serie de tiempo
        List<Punto> serie = jdbc.query(BASE + """
                SELECT CAST(date_trunc(:unidad, local) AS date) AS f, SUM(n) AS c, COUNT(DISTINCT operacion_id) AS v
                FROM base GROUP BY 1 ORDER BY 1
                """, p, (rs, i) -> new Punto(rs.getObject("f", LocalDate.class), rs.getInt("c"), rs.getInt("v")));

        return new ConsolidadoDemandaResponse(ini, fin, embarcacion, origen, unidad, total, viajes,
                viajes == 0 ? null : BigDecimal.valueOf(total).divide(BigDecimal.valueOf(viajes), 1, RoundingMode.HALF_UP),
                new Suspendidos(entero(t.get("viajes_susp")), entero(t.get("usuarios_susp"))),
                porSexo, porGrupo, porOrigen, porDestino, od, porFranja, porHora, serie);
    }

    private static <K extends Enum<K>> Map<K, Integer> vacio(Class<K> tipo) {
        Map<K, Integer> m = new EnumMap<>(tipo);
        for (K k : tipo.getEnumConstants()) {
            m.put(k, 0);
        }
        return m;
    }

    private static int entero(Object v) {
        return v == null ? 0 : ((Number) v).intValue();
    }
}
