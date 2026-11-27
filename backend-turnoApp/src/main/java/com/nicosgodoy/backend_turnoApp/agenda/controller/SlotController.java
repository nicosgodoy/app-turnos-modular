package com.nicosgodoy.backend_turnoApp.agenda.controller;

import com.nicosgodoy.backend_turnoApp.agenda.dto.SlotResponse;
import com.nicosgodoy.backend_turnoApp.agenda.service.SlotService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medicos/{medicoId}/slots")
@RequiredArgsConstructor
public class SlotController {

	private final SlotService slotService;

	@GetMapping
	public List<SlotResponse> listar(
			@PathVariable UUID medicoId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return slotService.slots(medicoId, fecha == null ? LocalDate.now() : fecha);
	}
}
