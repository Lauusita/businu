package com.lau.api.dto.infraestructura;

import com.lau.api.dominio.enums.Embarcaderos;
import com.lau.api.dominio.enums.seguimiento.ColorLuz;
import com.lau.api.dominio.enums.seguimiento.EstructuraAyuda;
import com.lau.api.dominio.enums.seguimiento.FuncionMarca;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CrearAyudaNavegacionRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotNull EstructuraAyuda estructura,
        @NotNull FuncionMarca funcion,
        @NotNull Boolean luminosa,
        ColorLuz colorLuz,                         // obligatorio solo si luminosa = true (lo valida la entidad)
        @Size(max = 40) String ritmoLuz,           // p. ej. "Fl. R 3 s"
        @Size(max = 300) String descripcion,       // obligatoria si estructura = OTRA
        @NotNull Embarcaderos tramoDesde,
        @NotNull Embarcaderos tramoHasta,
        @NotBlank @Size(max = 200) String referenciaUbicacion,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal latitud,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal longitud) {
}