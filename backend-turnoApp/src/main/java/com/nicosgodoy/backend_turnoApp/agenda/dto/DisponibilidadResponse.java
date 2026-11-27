package com.nicosgodoy.backend_turnoApp.agenda.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DisponibilidadResponse(
		UUID id,
		UUID medicoId,
		UUID consultorioId,
		String consultorio,
		int diaSemana,
		LocalTime horaDesde,
		LocalTime horaHasta,
		LocalDate vigenteDesde,
		LocalDate vigenteHasta,
		boolean activo) {
}
