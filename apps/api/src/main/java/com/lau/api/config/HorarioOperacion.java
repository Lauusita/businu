package com.lau.api.config;

import com.lau.api.exceptions.BusinessException;
import java.time.*;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HorarioOperacion {

    private final ZoneId zona;
    private final LocalTime inicio;
    private final LocalTime fin;
    private final Set<DayOfWeek> dias;

    public HorarioOperacion(@Value("${operacion.zona}") String zona,
                            @Value("${operacion.hora-inicio}") LocalTime inicio,
                            @Value("${operacion.hora-fin}") LocalTime fin,
                            @Value("${operacion.dias}") Set<DayOfWeek> dias) {
        this.zona = ZoneId.of(zona);
        this.inicio = inicio;
        this.fin = fin;
        this.dias = dias;
    }

    /** El zarpe debe empezar en un día y una franja habilitados. */
    public void exigirZarpePermitido(Instant momento) {
        ZonedDateTime local = momento.atZone(zona);
        if (!dias.contains(local.getDayOfWeek())) {
            throw new BusinessException("No hay operación ordinaria los " + nombreDia(local.getDayOfWeek())
                    + " (anexo §7.1.1 y §7.1.9)");
        }
        LocalTime hora = local.toLocalTime();
        if (hora.isBefore(inicio) || !hora.isBefore(fin)) {
            throw new BusinessException("La operación ordinaria es de " + inicio + " a " + fin
                    + " (hora de Colombia). Hora indicada: " + hora.withNano(0));
        }
    }

    private static String nombreDia(DayOfWeek d) {
        return d.getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "CO"));
    }
}