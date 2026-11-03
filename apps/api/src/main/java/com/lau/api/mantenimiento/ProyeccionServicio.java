package com.lau.api.mantenimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Próximo servicio periódico de un motor. Se calcula al consultar, no se guarda, porque depende
 * del horómetro actual.
 *
 * @param fechaAlcanza100h  día operativo en que se alcanza el horómetro objetivo (estimado)
 * @param fechaSugerida     último sábado antes de superar el horómetro objetivo (§7.1.8: los sábados son de mantenimiento)
 * @param fechaLimite       la que ocurra primero entre la fecha sugerida y 12 meses desde el último servicio
 */
public record ProyeccionServicio(
        EstadoProyeccion estado,
        BigDecimal horometroActual,
        BigDecimal proximoHorometro,
        BigDecimal horasRestantes,
        Integer diasOperativosRestantes,
        LocalDate fechaAlcanza100h,
        LocalDate fechaSugerida,
        LocalDate fechaLimite) {

    public static ProyeccionServicio sinServicio(BigDecimal horometroActual) {
        return new ProyeccionServicio(EstadoProyeccion.SIN_SERVICIO_REGISTRADO, horometroActual,
                null, null, null, null, null, null);
    }
}
