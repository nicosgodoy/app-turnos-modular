package com.nicosgodoy.backend_turnoApp.turno.controller;

import com.nicosgodoy.backend_turnoApp.shared.security.SecurityUtils;
import com.nicosgodoy.backend_turnoApp.turno.dto.CancelarTurnoRequest;
import com.nicosgodoy.backend_turnoApp.turno.dto.ReprogramarTurnoRequest;
import com.nicosgodoy.backend_turnoApp.turno.dto.ReservarTurnoRequest;
import com.nicosgodoy.backend_turnoApp.turno.dto.TurnoResponse;
import com.nicosgodoy.backend_turnoApp.turno.service.TurnoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/turnos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PACIENTE')")
public class TurnoController {

	private final TurnoService turnoService;

	@PostMapping
	public TurnoResponse reservar(@Valid @RequestBody ReservarTurnoRequest request) {
		return turnoService.reservar(SecurityUtils.actual().id(), request);
	}

	@GetMapping("/mios")
	public List<TurnoResponse> misTurnos() {
		return turnoService.misTurnos(SecurityUtils.actual().id());
	}

	@PatchMapping("/{id}/cancelar")
	public TurnoResponse cancelar(@PathVariable UUID id,
			@Valid @RequestBody(required = false) CancelarTurnoRequest request) {
		return turnoService.cancelar(SecurityUtils.actual().id(), id,
				request == null ? null : request.motivo());
	}

	@PatchMapping("/{id}/reprogramar")
	public TurnoResponse reprogramar(@PathVariable UUID id,
			@Valid @RequestBody ReprogramarTurnoRequest request) {
		return turnoService.reprogramar(SecurityUtils.actual().id(), id, request.fechaHora());
	}
}
