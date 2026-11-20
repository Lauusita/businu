package com.lau.api.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginChangePassword(@NotBlank String numeroIdentificacion, @Email String email) {
}
