package com.nicosgodoy.backend_turnoApp.medico.controller;

import com.nicosgodoy.backend_turnoApp.medico.dto.EspecialidadRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.EspecialidadResponse;
import com.nicosgodoy.backend_turnoApp.medico.service.EspecialidadService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/especialidades")
@RequiredArgsConstructor
public class AdminEspecialidadController {

	private final EspecialidadService especialidadService;

	@GetMapping
	public List<EspecialidadResponse> listar() {
		return especialidadService.listarTodas();
	}

	@PostMapping
	public EspecialidadResponse crear(@Valid @RequestBody EspecialidadRequest request) {
		return especialidadService.crear(request);
	}

	@PutMapping("/{id}")
	public EspecialidadResponse actualizar(@PathVariable UUID id, @Valid @RequestBody EspecialidadRequest request,
			@RequestParam(required = false) Boolean activo) {
		return especialidadService.actualizar(id, request, activo);
	}
}
