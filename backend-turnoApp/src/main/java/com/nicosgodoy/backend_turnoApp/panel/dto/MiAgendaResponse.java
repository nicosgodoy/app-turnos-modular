package com.nicosgodoy.backend_turnoApp.panel.dto;

import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadResponse;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ExcepcionResponse;
import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResponse;
import java.util.List;

public record MiAgendaResponse(
		MedicoResponse perfil,
		List<DisponibilidadResponse> disponibilidad,
		List<ExcepcionResponse> excepciones) {
}
