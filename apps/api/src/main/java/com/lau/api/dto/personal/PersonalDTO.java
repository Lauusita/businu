package com.lau.api.dto.personal;

import com.lau.api.dominio.enums.Rol;
import com.lau.api.dominio.enums.TipoIdentificacion;
import com.lau.api.dominio.enums.TipoVinculacion;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record   PersonalDTO(
        @NotNull(message = "El tipo de identificación es obligatorio")
        TipoIdentificacion tipoIdentificacion,

        @NotBlank(message = "El número de identificación es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{5,20}$",
                message = "El número de identificación debe tener entre 5 y 20 letras o números, sin puntos ni espacios")
        String numeroIdentificacion,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, max = 80, message = "Los nombres deben tener entre 2 y 80 caracteres")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "Los nombres solo pueden contener letras, espacios, apóstrofo y guion")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 80, message = "Los apellidos deben tener entre 2 y 80 caracteres")
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*$",
                message = "Los apellidos solo pueden contener letras, espacios, apóstrofo y guion")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 120, message = "El correo no puede superar 120 caracteres")
        String email,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        @NotNull(message = "El tipo de vinculación es obligatorio")
        TipoVinculacion tipoVinculacion,
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {

    /** Los guías no tienen acceso; los demás roles necesitan contraseña. */
    @AssertTrue(message = "La contraseña es obligatoria para este rol y no se permite para guías")
    public boolean isAccesoCoherenteConRol() {
        if (rol == null) {
            return true;  // ya lo reporta @NotNull
        }
        return rol.usaSistema() == (password != null);
    }
}