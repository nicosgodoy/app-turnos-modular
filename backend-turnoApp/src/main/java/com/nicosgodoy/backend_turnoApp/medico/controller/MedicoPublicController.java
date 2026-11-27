package com.nicosgodoy.backend_turnoApp.medico.controller;

import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResumenResponse;
import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResponse;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medicos")
@RequiredArgsConstructor
public class MedicoPublicController {

	private final MedicoService medicoService;

	@GetMapping
	public List<MedicoResumenResponse> buscar(
			@RequestParam(required = false) UUID especialidadId,
			@RequestParam(required = false) UUID obraSocialId,
			@RequestParam(required = false) UUID planId) {
		return medicoService.buscar(especialidadId, obraSocialId, planId);
	}

	@GetMapping("/{id}")
	public MedicoResponse obtener(@PathVariable UUID id) {
		return medicoService.obtenerResponse(id);
	}
}
