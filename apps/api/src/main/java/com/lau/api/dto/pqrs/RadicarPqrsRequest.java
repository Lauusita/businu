package com.lau.api.dto.pqrs;

import com.lau.api.dominio.enums.operacion.Embarcaderos;
import com.lau.api.dominio.enums.pqrs.TipoPqrs;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Formulario público (código QR). "sitioWeb" es un campo trampa: las personas no lo ven ni lo llenan. */
public record RadicarPqrsRequest(
        @NotNull TipoPqrs tipo,
        @NotBlank @Size(min = 10, max = 3000) String descripcion,
        Embarcaderos embarcadero,
        @PastOrPresent LocalDate fechaHecho,
        @Size(max = 150) String nombre,
        @Email @Size(max = 150) String correo,
        @Pattern(regexp = "^[0-9 +()-]{7,30}$", message = "Teléfono inválido") String telefono,
        Boolean autorizaDatos,          // ausente = no autoriza
        String sitioWeb) {
}
