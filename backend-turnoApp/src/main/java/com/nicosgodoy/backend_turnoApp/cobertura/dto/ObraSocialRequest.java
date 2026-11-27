package com.nicosgodoy.backend_turnoApp.cobertura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObraSocialRequest(@NotBlank @Size(max = 120) String nombre, @NotBlank @Size(max = 40) String codigo) {
}
