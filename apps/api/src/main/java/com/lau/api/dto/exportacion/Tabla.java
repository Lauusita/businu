package com.lau.api.dto.exportacion;

import java.util.List;

/** Un conjunto de datos listo para exportar: una hoja de Excel o un archivo CSV. */
public record Tabla(String nombre, List<String> columnas, List<List<Object>> filas) {
}
