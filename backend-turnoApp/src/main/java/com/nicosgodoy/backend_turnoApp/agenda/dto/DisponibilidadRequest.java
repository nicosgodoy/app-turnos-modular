package com.nicosgodoy.backend_turnoApp.agenda.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DisponibilidadRequest(
		@NotNull UUID consultorioId,
		@Min(1) @Max(7) int diaSemana,
		@NotNull LocalTime horaDesde,
		@NotNull LocalTime horaHasta,
		@NotNull LocalDate vigenteDesde,
		LocalDate vigenteHasta) {
}
