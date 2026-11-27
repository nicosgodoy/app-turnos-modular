package com.nicosgodoy.backend_turnoApp.medico.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MedicoResumenResponse(
		UUID id,
		String nombre,
		String apellido,
		String matricula,
		String biografia,
		int duracionTurnoMin,
		BigDecimal precioParticular,
		List<EspecialidadResponse> especialidades) {
}
