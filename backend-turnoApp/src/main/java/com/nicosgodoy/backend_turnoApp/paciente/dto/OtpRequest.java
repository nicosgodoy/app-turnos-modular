package com.nicosgodoy.backend_turnoApp.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record OtpRequest(
		@NotBlank @Size(max = 15) String dni,
		@NotBlank @Size(max = 120) String apellido,
		@Size(max = 120) String nombre,
		@Email @Size(max = 255) String email,
		@Size(max = 30) String telefono,
		LocalDate fechaNacimiento,
		UUID obraSocialId,
		UUID planId,
		@Size(max = 50) String numeroAfiliado,
		Boolean consentimiento) {
}
