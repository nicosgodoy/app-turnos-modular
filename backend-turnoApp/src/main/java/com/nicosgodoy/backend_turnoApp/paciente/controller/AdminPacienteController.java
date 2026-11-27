package com.nicosgodoy.backend_turnoApp.paciente.controller;

import com.nicosgodoy.backend_turnoApp.paciente.dto.ActualizarPacienteRequest;
import com.nicosgodoy.backend_turnoApp.paciente.dto.PacienteResponse;
import com.nicosgodoy.backend_turnoApp.paciente.service.PacienteService;
import com.nicosgodoy.backend_turnoApp.shared.web.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/pacientes")
@RequiredArgsConstructor
public class AdminPacienteController {

	private final PacienteService pacienteService;

	@GetMapping
	public PageResponse<PacienteResponse> listar(
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		var resultado = pacienteService.buscar(q, PageRequest.of(page, Math.min(size, 100),
				Sort.by("apellido", "nombre")));
		return PageResponse.de(resultado, pacienteService::aResponse);
	}

	@GetMapping("/{id}")
	public PacienteResponse obtener(@PathVariable UUID id) {
		return pacienteService.aResponse(pacienteService.obtener(id));
	}

	@PutMapping("/{id}")
	public PacienteResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarPacienteRequest request) {
		return pacienteService.aResponse(pacienteService.actualizar(id, request));
	}
}
