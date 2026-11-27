package com.nicosgodoy.backend_turnoApp.agenda.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExcepcionRequest(
		@NotNull LocalDate fechaDesde,
		@NotNull LocalDate fechaHasta,
		Boolean todoElDia,
		LocalTime horaDesde,
		LocalTime horaHasta,
		@Size(max = 255) String motivo) {
}
