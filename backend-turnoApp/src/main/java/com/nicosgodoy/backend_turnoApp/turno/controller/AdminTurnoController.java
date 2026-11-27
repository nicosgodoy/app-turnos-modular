package com.nicosgodoy.backend_turnoApp.turno.controller;

import com.nicosgodoy.backend_turnoApp.shared.web.PageResponse;
import com.nicosgodoy.backend_turnoApp.turno.dto.CambiarEstadoTurnoRequest;
import com.nicosgodoy.backend_turnoApp.turno.dto.TurnoResponse;
import com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno;
import com.nicosgodoy.backend_turnoApp.turno.service.TurnoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/turnos")
@RequiredArgsConstructor
public class AdminTurnoController {

	private final TurnoService turnoService;

	@GetMapping
	public PageResponse<TurnoResponse> listar(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
			@RequestParam(required = false) UUID medicoId,
			@RequestParam(required = false) EstadoTurno estado,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return turnoService.listarAdmin(fecha, medicoId, estado, page, size);
	}

	@GetMapping("/{id}")
	public TurnoResponse obtener(@PathVariable UUID id) {
		return turnoService.obtener(id);
	}

	@PatchMapping("/{id}/estado")
	public TurnoResponse cambiarEstado(@PathVariable UUID id,
			@Valid @RequestBody CambiarEstadoTurnoRequest request) {
		return turnoService.cambiarEstado(id, request.estado(), request.motivo());
	}

	@PatchMapping("/{id}/cancelar")
	public TurnoResponse cancelar(@PathVariable UUID id, @RequestParam(required = false) String motivo) {
		return turnoService.cancelarPorClinica(id, motivo);
	}
}
