package com.nicosgodoy.backend_turnoApp.medico.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadRequest(
		@NotBlank @Size(max = 120) String nombre,
		@Min(5) @Max(240) int duracionDefaultMin) {
}
