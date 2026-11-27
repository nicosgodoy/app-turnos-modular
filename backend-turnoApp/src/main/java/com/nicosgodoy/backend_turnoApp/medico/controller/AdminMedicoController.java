package com.nicosgodoy.backend_turnoApp.medico.controller;

import com.nicosgodoy.backend_turnoApp.medico.dto.ActualizarMedicoRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.CrearMedicoRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResponse;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/medicos")
@RequiredArgsConstructor
public class AdminMedicoController {

	private final MedicoService medicoService;

	@GetMapping
	public List<MedicoResponse> listar() {
		return medicoService.listarTodos();
	}

	@GetMapping("/{id}")
	public MedicoResponse obtener(@PathVariable UUID id) {
		return medicoService.obtenerResponse(id);
	}

	@PostMapping
	public MedicoResponse crear(@Valid @RequestBody CrearMedicoRequest request) {
		return medicoService.crear(request);
	}

	@PutMapping("/{id}")
	public MedicoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarMedicoRequest request) {
		return medicoService.actualizar(id, request);
	}

	@PatchMapping("/{id}/estado")
	public MedicoResponse cambiarEstado(@PathVariable UUID id, @RequestParam boolean activo) {
		return medicoService.cambiarEstado(id, activo);
	}
}
