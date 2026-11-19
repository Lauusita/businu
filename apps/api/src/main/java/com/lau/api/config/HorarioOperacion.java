package com.lau.api.config;

import com.lau.api.exceptions.BusinessException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Horario de prestación del servicio: 05:00 a 21:00 (§7.1.1). Días: lunes a domingo por decisión de
 * negocio (el anexo indica L-V); se configuran en operacion.dias.
 * Se configura en application.properties con el prefijo "operacion".
 *
 * @param toleranciaCierre margen después de horaFin para registrar llegadas, paradas y cierres
 *                         de un viaje que zarpó dentro del horario (p. ej. zarpe 20:45, llegada 21:20).
 */
@ConfigurationProperties(prefix = "operacion")
public record HorarioOperacion(
        ZoneId zona,
        LocalTime horaInicio,
        LocalTime horaFin,
        Set<DayOfWeek> dias,
        Duration toleranciaCierre) {

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    public HorarioOperacion {
        zona = zona != null ? zona : ZoneId.of("America/Bogota");
        horaInicio = horaInicio != null ? horaInicio : LocalTime.of(5, 0);
        horaFin = horaFin != null ? horaFin : LocalTime.of(21, 0);
        dias = (dias == null || dias.isEmpty())
                ? EnumSet.allOf(DayOfWeek.class)
                : EnumSet.copyOf(dias);
        toleranciaCierre = toleranciaCierre != null ? toleranciaCierre : Duration.ofMinutes(30);
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalStateException("operacion.hora-inicio debe ser anterior a operacion.hora-fin");
        }
    }

    /** El zarpe debe ser un día de servicio, entre horaInicio (incluida) y horaFin (excluida). */
    public void exigirZarpePermitido(Instant inicio) {
        ZonedDateTime local = inicio.atZone(zona);
        LocalTime hora = local.toLocalTime();
        if (!dias.contains(local.getDayOfWeek()) || hora.isBefore(horaInicio) || !hora.isBefore(horaFin)) {
            throw new BusinessException("No se puede zarpar el " + nombreDia(local.getDayOfWeek()) + " a las "
                    + hora.withSecond(0).withNano(0) + ". " + descripcion());
        }
    }

    /**
     * Paradas, llegada y suspensión deben ocurrir la misma jornada del zarpe, a más tardar
     * horaFin + toleranciaCierre. Evita viajes de varias horas o días por un error de digitación.
     */
    public void exigirDentroDeJornada(Instant inicio, Instant momento, String queSeRegistra) {
        ZonedDateTime limite = inicio.atZone(zona).toLocalDate().atTime(horaFin).plus(toleranciaCierre).atZone(zona);
        if (momento.isAfter(limite.toInstant())) {
            throw new BusinessException("La " + queSeRegistra + " debe registrarse el mismo día del zarpe, a más tardar a las "
                    + limite.toLocalTime() + " (hora de Colombia)");
        }
    }

    public String descripcion() {
        String nombres = dias.stream().map(HorarioOperacion::nombreDia).collect(Collectors.joining(", "));
        return "Horario de operación: " + nombres + " de " + horaInicio + " a " + horaFin + " (hora de Colombia)";
    }

    private static String nombreDia(DayOfWeek d) {
        return d.getDisplayName(TextStyle.FULL, ES);
    }
}