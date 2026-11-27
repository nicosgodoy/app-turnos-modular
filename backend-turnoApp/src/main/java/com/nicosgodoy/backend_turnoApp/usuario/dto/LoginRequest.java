package com.nicosgodoy.backend_turnoApp.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
}
