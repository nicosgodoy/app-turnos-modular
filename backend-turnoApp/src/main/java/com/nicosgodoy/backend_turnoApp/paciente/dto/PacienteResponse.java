package com.nicosgodoy.backend_turnoApp.paciente.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PacienteResponse(
		UUID id,
		String nombre,
		String apellido,
		String dni,
		LocalDate fechaNacimiento,
		String telefono,
		String email,
		CoberturaResumen obraSocial,
		CoberturaResumen plan,
		String numeroAfiliado,
		boolean consentimientoDatos) {

	public record CoberturaResumen(UUID id, String nombre) {
	}
}
