package com.lau.api.utilidades;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos para las plantillas PDF (hora de Colombia, números en español). */
public class Formato {

    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String fecha(Instant i) {
        return i == null ? "—" : FECHA.format(i.atZone(COLOMBIA));
    }

    public String fechaHora(Instant i) {
        return i == null ? "—" : FECHA_HORA.format(i.atZone(COLOMBIA));
    }

    public String numero(Object n) {
        if (n == null) {
            return "—";
        }
        NumberFormat f = NumberFormat.getNumberInstance(Locale.of("es", "CO"));
        f.setMaximumFractionDigits(2);
        return f.format(n instanceof BigDecimal b ? b.setScale(2, RoundingMode.HALF_UP) : n);
    }

    /** Valor de un mapa por el nombre de la clave (las claves pueden ser enums). */
    public Object valor(java.util.Map<?, ?> mapa, String clave) {
        if (mapa == null) {
            return null;
        }
        return mapa.entrySet().stream().filter(e -> String.valueOf(e.getKey()).equals(clave))
                .map(java.util.Map.Entry::getValue).findFirst().orElse(null);
    }

    /** Celda de una tabla: números con formato, lo demás como texto. */
    public String celda(Object o) {
        return o instanceof Number ? numero(o) : texto(o);
    }

    public String texto(Object o) {
        return o == null ? "" : o.toString();
    }
}
