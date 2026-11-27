package com.nicosgodoy.backend_turnoApp.panel.controller;

import com.nicosgodoy.backend_turnoApp.agenda.service.AgendaService;
import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResponse;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import com.nicosgodoy.backend_turnoApp.panel.dto.MiAgendaResponse;
import com.nicosgodoy.backend_turnoApp.shared.security.SecurityUtils;
import com.nicosgodoy.backend_turnoApp.turno.dto.TurnoResponse;
import com.nicosgodoy.backend_turnoApp.turno.service.TurnoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medico")
@RequiredArgsConstructor
public class MedicoPanelController {

	private final MedicoService medicoService;
	private final AgendaService agendaService;
	private final TurnoService turnoService;

	@GetMapping("/perfil")
	public MedicoResponse perfil() {
		return medicoService.perfil(SecurityUtils.actual().id());
	}

	@GetMapping("/agenda")
	public MiAgendaResponse agenda() {
		var medico = medicoService.porUsuario(SecurityUtils.actual().id());
		return new MiAgendaResponse(
				medicoService.aResponse(medico),
				agendaService.listarDisponibilidad(medico.getId()),
				agendaService.listarExcepciones(medico.getId()));
	}

	@GetMapping("/turnos")
	public List<TurnoResponse> turnos(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return turnoService.turnosDelMedico(SecurityUtils.actual().id(), fecha);
	}
}
