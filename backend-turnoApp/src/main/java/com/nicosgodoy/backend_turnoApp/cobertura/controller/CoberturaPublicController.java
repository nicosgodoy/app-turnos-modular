package com.nicosgodoy.backend_turnoApp.cobertura.controller;

import com.nicosgodoy.backend_turnoApp.cobertura.dto.ObraSocialResponse;
import com.nicosgodoy.backend_turnoApp.cobertura.service.CoberturaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/obras-sociales")
@RequiredArgsConstructor
public class CoberturaPublicController {

	private final CoberturaService coberturaService;

	@GetMapping
	public List<ObraSocialResponse> listar() {
		return coberturaService.listarActivas();
	}
}
