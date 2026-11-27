package com.nicosgodoy.backend_turnoApp.medico.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MedicoResponse(
		UUID id,
		String nombre,
		String apellido,
		String email,
		String matricula,
		String biografia,
		int duracionTurnoMin,
		BigDecimal precioParticular,
		boolean activo,
		List<EspecialidadResponse> especialidades,
		List<CoberturaResponse> coberturas) {
}
