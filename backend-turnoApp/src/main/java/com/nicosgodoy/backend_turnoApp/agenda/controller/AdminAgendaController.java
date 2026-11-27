package com.nicosgodoy.backend_turnoApp.agenda.controller;

import com.nicosgodoy.backend_turnoApp.agenda.dto.ConsultorioRequest;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ConsultorioResponse;
import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadRequest;
import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadResponse;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ExcepcionRequest;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ExcepcionResponse;
import com.nicosgodoy.backend_turnoApp.agenda.service.AgendaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class AdminAgendaController {

	private final AgendaService agendaService;

	@GetMapping("/consultorios")
	public List<ConsultorioResponse> listarConsultorios() {
		return agendaService.listarConsultorios();
	}

	@PostMapping("/consultorios")
	public ConsultorioResponse crearConsultorio(@Valid @RequestBody ConsultorioRequest request) {
		return agendaService.crearConsultorio(request);
	}

	@PutMapping("/consultorios/{id}")
	public ConsultorioResponse actualizarConsultorio(@PathVariable UUID id,
			@Valid @RequestBody ConsultorioRequest request,
			@RequestParam(required = false) Boolean activo) {
		return agendaService.actualizarConsultorio(id, request, activo);
	}

	@GetMapping("/medicos/{medicoId}/disponibilidad")
	public List<DisponibilidadResponse> listarDisponibilidad(@PathVariable UUID medicoId) {
		return agendaService.listarDisponibilidad(medicoId);
	}

	@PostMapping("/medicos/{medicoId}/disponibilidad")
	public DisponibilidadResponse crearDisponibilidad(@PathVariable UUID medicoId,
			@Valid @RequestBody DisponibilidadRequest request) {
		return agendaService.crearDisponibilidad(medicoId, request);
	}

	@PutMapping("/disponibilidad/{id}")
	public DisponibilidadResponse actualizarDisponibilidad(@PathVariable UUID id,
			@Valid @RequestBody DisponibilidadRequest request,
			@RequestParam(required = false) Boolean activo) {
		return agendaService.actualizarDisponibilidad(id, request, activo);
	}

	@PatchMapping("/disponibilidad/{id}/estado")
	public DisponibilidadResponse cambiarEstadoDisponibilidad(@PathVariable UUID id,
			@RequestParam boolean activo) {
		return agendaService.cambiarEstadoDisponibilidad(id, activo);
	}

	@GetMapping("/medicos/{medicoId}/excepciones")
	public List<ExcepcionResponse> listarExcepciones(@PathVariable UUID medicoId) {
		return agendaService.listarExcepciones(medicoId);
	}

	@PostMapping("/medicos/{medicoId}/excepciones")
	public ExcepcionResponse crearExcepcion(@PathVariable UUID medicoId,
			@Valid @RequestBody ExcepcionRequest request) {
		return agendaService.crearExcepcion(medicoId, request);
	}

	@DeleteMapping("/excepciones/{id}")
	public ResponseEntity<Void> eliminarExcepcion(@PathVariable UUID id) {
		agendaService.eliminarExcepcion(id);
		return ResponseEntity.noContent().build();
	}
}
