package com.nicosgodoy.backend_turnoApp.paciente.controller;

import com.nicosgodoy.backend_turnoApp.paciente.dto.ActualizarMisDatosRequest;
import com.nicosgodoy.backend_turnoApp.paciente.dto.PacienteResponse;
import com.nicosgodoy.backend_turnoApp.paciente.service.PacienteService;
import com.nicosgodoy.backend_turnoApp.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

	private final PacienteService pacienteService;

	@GetMapping("/me")
	@PreAuthorize("hasRole('PACIENTE')")
	public PacienteResponse misDatos() {
		return pacienteService.aResponse(
				pacienteService.obtenerPorUsuario(SecurityUtils.actual().id()));
	}

	@PutMapping("/me")
	@PreAuthorize("hasRole('PACIENTE')")
	public PacienteResponse actualizarMisDatos(@Valid @RequestBody ActualizarMisDatosRequest request) {
		var paciente = pacienteService.actualizarMisDatos(SecurityUtils.actual().id(),
				request.telefono(), request.obraSocialId(), request.planId(), request.numeroAfiliado());
		return pacienteService.aResponse(paciente);
	}
}
