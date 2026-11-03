package co.gov.monteria.businu.mantenimiento;

import co.gov.monteria.businu.compartido.CalendarioOperativo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Component;

/**
 * Proyecta el próximo servicio de 100 h de un motor.
 *
 * <pre>
 * horas_restantes = proximo_horometro − horometro_actual
 * dias_operativos = ceil(horas_restantes / horas_operacion_dia)      (6 recorridos × 1 h)
 * fecha_100h      = hoy + dias_operativos (lunes a viernes, sin festivos)
 * fecha_sugerida  = último sábado antes del primer día en que se superaría el horómetro objetivo
 * fecha_limite    = min(fecha_sugerida, último servicio + 12 meses)
 * </pre>
 *
 * Se asume que el horómetro actual ya incluye las horas de hoy, por eso se cuenta desde mañana.
 */
@Component
public class CalculadoraProximoServicio {

    /** Una semana de operación (30 h): da tiempo de programar el sábado y conseguir repuestos. */
    public static final BigDecimal UMBRAL_ALERTA_HORAS = new BigDecimal("30");
    public static final int MESES_MAXIMOS_ENTRE_SERVICIOS = 12;

    private final CalendarioOperativo calendario;

    public CalculadoraProximoServicio(CalendarioOperativo calendario) {
        this.calendario = calendario;
    }

    public ProyeccionServicio proyectar(BigDecimal proximoHorometro, BigDecimal horometroActual,
                                        BigDecimal horasOperacionDia, LocalDate fechaUltimoServicio,
                                        LocalDate hoy) {
        LocalDate limiteCalendario = fechaUltimoServicio.plusMonths(MESES_MAXIMOS_ENTRE_SERVICIOS);
        BigDecimal horasRestantes = proximoHorometro.subtract(horometroActual);

        if (horasRestantes.signum() <= 0 || !hoy.isBefore(limiteCalendario)) {
            return new ProyeccionServicio(EstadoProyeccion.VENCIDO, horometroActual, proximoHorometro,
                    horasRestantes, 0, null, null, limiteCalendario.isBefore(hoy) ? limiteCalendario : hoy);
        }

        int dias = horasRestantes.divide(horasOperacionDia, 0, RoundingMode.CEILING).intValueExact();
        LocalDate fecha100h = calendario.sumarDiasOperativos(hoy, dias);

        // Primer día operativo en el que, si se navega, se superaría el horómetro objetivo.
        int diasCompletos = horasRestantes.divide(horasOperacionDia, 0, RoundingMode.FLOOR).intValueExact();
        LocalDate fechaExcede = calendario.sumarDiasOperativos(hoy, diasCompletos + 1);
        LocalDate sabado = fechaExcede.with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
        // Si ese sábado ya pasó (quedan menos horas que una jornada), el servicio es urgente: hoy.
        LocalDate sugerida = sabado.isBefore(hoy) ? hoy : sabado;
        LocalDate limite = sugerida.isBefore(limiteCalendario) ? sugerida : limiteCalendario;

        EstadoProyeccion estado = horasRestantes.compareTo(UMBRAL_ALERTA_HORAS) <= 0
                || !limite.isAfter(hoy.plusDays(7))
                ? EstadoProyeccion.PROXIMO
                : EstadoProyeccion.AL_DIA;

        return new ProyeccionServicio(estado, horometroActual, proximoHorometro, horasRestantes, dias,
                fecha100h, sugerida, limite);
    }
}
