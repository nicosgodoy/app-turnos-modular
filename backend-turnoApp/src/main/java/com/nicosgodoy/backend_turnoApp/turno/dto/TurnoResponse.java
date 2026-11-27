package com.nicosgodoy.backend_turnoApp.turno.dto;

import com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno;
import com.nicosgodoy.backend_turnoApp.turno.entity.TipoCobertura;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TurnoResponse(
		UUID id,
		UUID medicoId,
		String medico,
		String especialidades,
		UUID pacienteId,
		String paciente,
		String pacienteDni,
		OffsetDateTime fechaHora,
		int duracionMin,
		String consultorio,
		EstadoTurno estado,
		TipoCobertura tipoCobertura,
		String obraSocial,
		String plan,
		String numeroAfiliado,
		BigDecimal precio,
		String motivo,
		boolean cancelable,
		Instant creadoEn) {
}
