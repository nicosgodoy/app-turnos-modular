package com.nicosgodoy.backend_turnoApp.medico.controller;

import com.nicosgodoy.backend_turnoApp.medico.dto.EspecialidadResponse;
import com.nicosgodoy.backend_turnoApp.medico.service.EspecialidadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/especialidades")
@RequiredArgsConstructor
public class EspecialidadPublicController {

	private final EspecialidadService especialidadService;

	@GetMapping
	public List<EspecialidadResponse> listar() {
		return especialidadService.listarActivas();
	}
}
