package com.lau.api.compartido;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Días de operación del piloto (§7.1.1): lunes a viernes, sin festivos. Los sábados se reservan
 * para mantenimiento preventivo (§7.1.8) y los domingos no hay servicio (§7.1.9).
 *
 * <p>Los festivos de Colombia se calculan (Ley 51 de 1983, "Ley Emiliani"), así no hay que
 * cargarlos a mano cada año.
 */
@Component
public class CalendarioOperativo {

    private final Map<Integer, Set<LocalDate>> cache = new ConcurrentHashMap<>();

    public boolean esDiaOperativo(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY && !esFestivo(fecha);
    }

    public boolean esFestivo(LocalDate fecha) {
        return festivos(fecha.getYear()).contains(fecha);
    }

    /** Suma N días operativos contando desde el día siguiente a {@code desde}. */
    public LocalDate sumarDiasOperativos(LocalDate desde, int dias) {
        LocalDate fecha = desde;
        int contados = 0;
        while (contados < dias) {
            fecha = fecha.plusDays(1);
            if (esDiaOperativo(fecha)) {
                contados++;
            }
        }
        return fecha;
    }

    public Set<LocalDate> festivos(int anio) {
        return cache.computeIfAbsent(anio, CalendarioOperativo::calcularFestivos);
    }

    private static Set<LocalDate> calcularFestivos(int anio) {
        Set<LocalDate> f = new TreeSet<>();
        // Fijos
        f.add(LocalDate.of(anio, 1, 1));    // Año Nuevo
        f.add(LocalDate.of(anio, 5, 1));    // Día del Trabajo
        f.add(LocalDate.of(anio, 7, 20));   // Independencia
        f.add(LocalDate.of(anio, 8, 7));    // Batalla de Boyacá
        f.add(LocalDate.of(anio, 12, 8));   // Inmaculada Concepción
        f.add(LocalDate.of(anio, 12, 25));  // Navidad
        // Se trasladan al lunes siguiente
        f.add(lunesSiguiente(LocalDate.of(anio, 1, 6)));    // Reyes Magos
        f.add(lunesSiguiente(LocalDate.of(anio, 3, 19)));   // San José
        f.add(lunesSiguiente(LocalDate.of(anio, 6, 29)));   // San Pedro y San Pablo
        f.add(lunesSiguiente(LocalDate.of(anio, 8, 15)));   // Asunción de la Virgen
        f.add(lunesSiguiente(LocalDate.of(anio, 10, 12)));  // Día de la Raza
        f.add(lunesSiguiente(LocalDate.of(anio, 11, 1)));   // Todos los Santos
        f.add(lunesSiguiente(LocalDate.of(anio, 11, 11)));  // Independencia de Cartagena
        // Dependen de la Pascua
        LocalDate pascua = domingoDePascua(anio);
        f.add(pascua.minusDays(3));                         // Jueves Santo
        f.add(pascua.minusDays(2));                         // Viernes Santo
        f.add(lunesSiguiente(pascua.plusDays(39)));         // Ascensión del Señor
        f.add(lunesSiguiente(pascua.plusDays(60)));         // Corpus Christi
        f.add(lunesSiguiente(pascua.plusDays(68)));         // Sagrado Corazón
        return Set.copyOf(f);
    }

    private static LocalDate lunesSiguiente(LocalDate fecha) {
        return fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /** Algoritmo de Meeus/Jones/Butcher para el calendario gregoriano. */
    static LocalDate domingoDePascua(int anio) {
        int a = anio % 19;
        int b = anio / 100;
        int c = anio % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(anio, mes, dia);
    }
}
