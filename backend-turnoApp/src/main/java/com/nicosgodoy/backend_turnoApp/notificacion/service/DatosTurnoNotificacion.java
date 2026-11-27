package com.nicosgodoy.backend_turnoApp.notificacion.service;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DatosTurnoNotificacion(
		UUID turnoId,
		String pacienteNombre,
		String pacienteEmail,
		String medicoNombre,
		OffsetDateTime fechaHora,
		String consultorio,
		String motivo) {
}
