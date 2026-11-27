package com.nicosgodoy.backend_turnoApp.paciente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
		@NotBlank String dni,
		@NotBlank @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos.") String codigo) {
}
