package com.nicosgodoy.backend_turnoApp.demo;

import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadRequest;
import com.nicosgodoy.backend_turnoApp.agenda.service.AgendaService;
import com.nicosgodoy.backend_turnoApp.cobertura.service.CoberturaService;
import com.nicosgodoy.backend_turnoApp.medico.dto.CoberturaRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.CrearMedicoRequest;
import com.nicosgodoy.backend_turnoApp.medico.service.EspecialidadService;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import com.nicosgodoy.backend_turnoApp.shared.config.DemoProperties;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

	private final DemoProperties demoProperties;
	private final MedicoService medicoService;
	private final EspecialidadService especialidadService;
	private final CoberturaService coberturaService;
	private final AgendaService agendaService;

	@Override
	public void run(ApplicationArguments args) {
		if (demoProperties.password() == null || demoProperties.password().isBlank()) {
			log.warn("Datos de demostración omitidos: falta APP_DEMO_PASSWORD.");
			return;
		}
		try {
			UUID laura = asegurarMedico("Laura", "Gómez", "laura.gomez@sagradafamilia.local", "MP-12345",
					"Clínica Médica", new BigDecimal("15000.00"),
					List.of(cobertura("OSDE", null), cobertura("IOMA", null), cobertura("PAMI", null)));
			UUID martin = asegurarMedico("Martín", "Pérez", "martin.perez@sagradafamilia.local", "MP-67890",
					"Pediatría", new BigDecimal("12000.00"),
					List.of(cobertura("SWISS", null), cobertura("OSDE", "210")));

			asegurarAgenda(laura, "Consultorio 1", List.of(
					new Bloque(1, LocalTime.of(9, 0), LocalTime.of(13, 0)),
					new Bloque(2, LocalTime.of(9, 0), LocalTime.of(13, 0)),
					new Bloque(3, LocalTime.of(9, 0), LocalTime.of(13, 0)),
					new Bloque(4, LocalTime.of(9, 0), LocalTime.of(13, 0)),
					new Bloque(5, LocalTime.of(9, 0), LocalTime.of(13, 0)),
					new Bloque(1, LocalTime.of(15, 0), LocalTime.of(19, 0)),
					new Bloque(3, LocalTime.of(15, 0), LocalTime.of(19, 0)),
					new Bloque(5, LocalTime.of(15, 0), LocalTime.of(19, 0))));

			asegurarAgenda(martin, "Consultorio 2", List.of(
					new Bloque(1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
					new Bloque(2, LocalTime.of(9, 0), LocalTime.of(12, 0)),
					new Bloque(3, LocalTime.of(9, 0), LocalTime.of(12, 0)),
					new Bloque(4, LocalTime.of(9, 0), LocalTime.of(12, 0)),
					new Bloque(5, LocalTime.of(9, 0), LocalTime.of(12, 0))));
		} catch (RuntimeException ex) {
			log.error("No se pudieron crear los datos de demostración", ex);
		}
	}

	private UUID asegurarMedico(String nombre, String apellido, String email, String matricula,
			String especialidad, BigDecimal precio, List<CoberturaRequest> coberturas) {
		var existente = medicoService.idPorMatricula(matricula);
		if (existente.isPresent()) {
			return existente.get();
		}
		try {
			var creado = medicoService.crear(new CrearMedicoRequest(
					nombre,
					apellido,
					email,
					demoProperties.password(),
					matricula,
					30,
					precio,
					"Profesional de demostración de " + especialidad + ".",
					List.of(especialidadService.idPorNombre(especialidad)),
					coberturas));
			log.info("Médico de demostración creado: {} {}", nombre, apellido);
			return creado.id();
		} catch (ApiException ex) {
			log.warn("No se pudo crear el médico {}: {}", matricula, ex.getMessage());
			return medicoService.idPorMatricula(matricula).orElseThrow(() -> ex);
		}
	}

	private void asegurarAgenda(UUID medicoId, String consultorio, List<Bloque> bloques) {
		if (!agendaService.listarDisponibilidad(medicoId).isEmpty()) {
			return;
		}
		UUID consultorioId = agendaService.idConsultorioPorNombre(consultorio);
		LocalDate desde = LocalDate.now();
		LocalDate hasta = desde.plusDays(120);
		for (Bloque bloque : bloques) {
			try {
				agendaService.crearDisponibilidad(medicoId, new DisponibilidadRequest(
						consultorioId, bloque.dia(), bloque.desde(), bloque.hasta(), desde, hasta));
			} catch (ApiException ex) {
				log.warn("No se pudo crear el bloque de agenda: {}", ex.getMessage());
			}
		}
	}

	private CoberturaRequest cobertura(String codigoObraSocial, String codigoPlan) {
		UUID obraSocialId = coberturaService.idObraSocialPorCodigo(codigoObraSocial);
		UUID planId = codigoPlan == null ? null : coberturaService.idPlanPorCodigo(obraSocialId, codigoPlan);
		return new CoberturaRequest(obraSocialId, planId);
	}

	private record Bloque(int dia, LocalTime desde, LocalTime hasta) {
	}
}
