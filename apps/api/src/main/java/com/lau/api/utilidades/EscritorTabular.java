package com.lau.api.utilidades;

import com.lau.api.dto.exportacion.Tabla;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

/** Escribe tablas en Excel (.xlsx) o CSV. */
@Component
public class EscritorTabular {

    /**
     * CSV en UTF-8 con BOM y separador ";" (Excel en español lo abre bien, porque la coma es el
     * separador decimal).
     */
    public byte[] csv(Tabla t) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(linea(t.columnas()));
        t.filas().forEach(f -> sb.append(linea(f)));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Un libro con una hoja por tabla: encabezado en negrilla, fijo y con filtros. */
    public byte[] xlsx(List<Tabla> tablas) {
        try (SXSSFWorkbook libro = new SXSSFWorkbook(200); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle encabezado = libro.createCellStyle();
            Font negrilla = libro.createFont();
            negrilla.setBold(true);
            encabezado.setFont(negrilla);
            encabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Tabla t : tablas) {
                SXSSFSheet hoja = libro.createSheet(t.nombre());
                Row fila0 = hoja.createRow(0);
                for (int c = 0; c < t.columnas().size(); c++) {
                    Cell celda = fila0.createCell(c);
                    celda.setCellValue(t.columnas().get(c));
                    celda.setCellStyle(encabezado);
                    hoja.setColumnWidth(c, Math.min(60, Math.max(12, t.columnas().get(c).length() + 4)) * 256);
                }
                int r = 1;
                for (List<Object> f : t.filas()) {
                    Row fila = hoja.createRow(r++);
                    for (int c = 0; c < f.size(); c++) {
                        Object v = f.get(c);
                        Cell celda = fila.createCell(c);
                        if (v instanceof Number n) {
                            celda.setCellValue(n.doubleValue());
                        } else if (v instanceof Boolean b) {
                            celda.setCellValue(b ? "Sí" : "No");
                        } else if (v != null) {
                            String texto = v.toString();
                            celda.setCellValue(texto.length() > 32000 ? texto.substring(0, 32000) : texto);
                        }
                    }
                }
                hoja.createFreezePane(0, 1);
                if (!t.columnas().isEmpty()) {
                    hoja.setAutoFilter(new CellRangeAddress(0, Math.max(0, r - 1), 0, t.columnas().size() - 1));
                }
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel", e);
        }
    }

    private static String linea(List<?> valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(';');
            }
            Object v = valores.get(i);
            String s = v == null ? "" : (v instanceof Boolean b ? (b ? "Sí" : "No") : v.toString());
            if (v instanceof Number) {
                s = s.replace('.', ',');                      // decimal con coma, como lo espera Excel en español
            }
            if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
                s = "\"" + s.replace("\"", "\"\"") + "\"";
            }
            sb.append(s);
        }
        return sb.append("\r\n").toString();
    }
}
