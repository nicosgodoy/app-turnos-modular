package com.nicosgodoy.backend_turnoApp.cobertura.controller;

import com.nicosgodoy.backend_turnoApp.cobertura.dto.ObraSocialRequest;
import com.nicosgodoy.backend_turnoApp.cobertura.dto.ObraSocialResponse;
import com.nicosgodoy.backend_turnoApp.cobertura.dto.PlanRequest;
import com.nicosgodoy.backend_turnoApp.cobertura.dto.PlanResponse;
import com.nicosgodoy.backend_turnoApp.cobertura.service.CoberturaService;
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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCoberturaController {

	private final CoberturaService coberturaService;

	@GetMapping("/obras-sociales")
	public List<ObraSocialResponse> listar() {
		return coberturaService.listarTodas();
	}

	@PostMapping("/obras-sociales")
	public ObraSocialResponse crear(@Valid @RequestBody ObraSocialRequest request) {
		return coberturaService.crearObraSocial(request);
	}

	@PutMapping("/obras-sociales/{id}")
	public ObraSocialResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ObraSocialRequest request,
			@RequestParam(required = false) Boolean activo) {
		return coberturaService.actualizarObraSocial(id, request, activo);
	}

	@PatchMapping("/obras-sociales/{id}/estado")
	public ObraSocialResponse cambiarEstado(@PathVariable UUID id, @RequestParam boolean activo) {
		return coberturaService.cambiarEstadoObraSocial(id, activo);
	}

	@PostMapping("/obras-sociales/{id}/planes")
	public PlanResponse crearPlan(@PathVariable UUID id, @Valid @RequestBody PlanRequest request) {
		return coberturaService.crearPlan(id, request);
	}

	@PutMapping("/planes/{id}")
	public PlanResponse actualizarPlan(@PathVariable UUID id, @Valid @RequestBody PlanRequest request,
			@RequestParam(required = false) Boolean activo) {
		return coberturaService.actualizarPlan(id, request, activo);
	}
}
