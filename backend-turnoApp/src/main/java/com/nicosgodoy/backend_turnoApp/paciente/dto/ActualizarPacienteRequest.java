package com.nicosgodoy.backend_turnoApp.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record ActualizarPacienteRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank @Size(max = 120) String apellido,
		LocalDate fechaNacimiento,
		@NotBlank @Size(max = 30) String telefono,
		@NotBlank @Email @Size(max = 255) String email,
		UUID obraSocialId,
		UUID planId,
		@Size(max = 50) String numeroAfiliado,
		Boolean consentimientoDatos) {
}
