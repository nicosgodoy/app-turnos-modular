package com.nicosgodoy.backend_turnoApp.agenda.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ExcepcionResponse(
		UUID id,
		UUID medicoId,
		LocalDate fechaDesde,
		LocalDate fechaHasta,
		boolean todoElDia,
		LocalTime horaDesde,
		LocalTime horaHasta,
		String motivo) {
}
