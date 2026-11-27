package com.nicosgodoy.backend_turnoApp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadRequest;
import com.nicosgodoy.backend_turnoApp.agenda.service.AgendaService;
import com.nicosgodoy.backend_turnoApp.cobertura.service.CoberturaService;
import com.nicosgodoy.backend_turnoApp.medico.dto.CoberturaRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.CrearMedicoRequest;
import com.nicosgodoy.backend_turnoApp.medico.service.EspecialidadService;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import com.nicosgodoy.backend_turnoApp.notificacion.service.EmailService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class TurnoFlujoIntegrationTest {

	private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
	private static final String DNI = "30111222";

	@Container
	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();
	@Autowired
	private MedicoService medicoService;
	@Autowired
	private EspecialidadService especialidadService;
	@Autowired
	private CoberturaService coberturaService;
	@Autowired
	private AgendaService agendaService;

	@MockitoBean
	private EmailService emailService;

	@Test
	void unPacienteReservaCancelaYElSlotVuelveAEstarLibre() throws Exception {
		UUID especialidadId = especialidadService.idPorNombre("Clínica Médica");
		UUID obraSocialId = coberturaService.idObraSocialPorCodigo("OSDE");
		var medico = medicoService.crear(new CrearMedicoRequest(
				"Test", "Doctor", "test.doctor@sagradafamilia.local", "password123", "MP-TEST-1",
				30, new BigDecimal("10000.00"), "Profesional de prueba",
				List.of(especialidadId), List.of(new CoberturaRequest(obraSocialId, null))));

		UUID consultorioId = agendaService.idConsultorioPorNombre("Consultorio 1");
		LocalDate fecha = LocalDate.now(ZONA).plusDays(3);
		agendaService.crearDisponibilidad(medico.id(), new DisponibilidadRequest(
				consultorioId, fecha.getDayOfWeek().getValue(),
				LocalTime.of(9, 0), LocalTime.of(12, 0), fecha, fecha));

		String slotsJson = mockMvc.perform(get("/medicos/{id}/slots", medico.id())
						.param("fecha", fecha.toString()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		JsonNode slots = objectMapper.readTree(slotsJson);
		assertThat(slots).isNotEmpty();
		String fechaHora = slots.get(0).get("fechaHora").asText();

		Map<String, Object> solicitudOtp = new HashMap<>();
		solicitudOtp.put("dni", DNI);
		solicitudOtp.put("apellido", "Paciente");
		solicitudOtp.put("nombre", "Ana");
		solicitudOtp.put("email", "ana@example.com");
		solicitudOtp.put("telefono", "1122334455");
		solicitudOtp.put("consentimiento", true);
		solicitudOtp.put("obraSocialId", obraSocialId.toString());
		mockMvc.perform(post("/auth/paciente/otp")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(solicitudOtp)))
				.andExpect(status().isOk());

		ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
		verify(emailService).enviarCodigoOtp(eq("ana@example.com"), eq("Ana"), codigo.capture(), any());

		String verificacionJson = mockMvc.perform(post("/auth/paciente/otp/verificar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"dni", DNI, "codigo", codigo.getValue()))))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String accessToken = objectMapper.readTree(verificacionJson).get("accessToken").asText();
		assertThat(accessToken).isNotBlank();

		Map<String, Object> reserva = new HashMap<>();
		reserva.put("medicoId", medico.id().toString());
		reserva.put("fechaHora", fechaHora);
		reserva.put("motivo", "Control");
		String turnoJson = mockMvc.perform(post("/turnos")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reserva)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CONFIRMADO"))
				.andExpect(jsonPath("$.tipoCobertura").value("OBRA_SOCIAL"))
				.andReturn().getResponse().getContentAsString();
		String turnoId = objectMapper.readTree(turnoJson).get("id").asText();

		mockMvc.perform(post("/turnos")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(reserva)))
				.andExpect(status().isConflict());

		mockMvc.perform(get("/turnos/mios")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(patch("/turnos/{id}/cancelar", turnoId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"motivo\":\"No puedo asistir\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("CANCELADO_PACIENTE"));

		String slotsLibresJson = mockMvc.perform(get("/medicos/{id}/slots", medico.id())
						.param("fecha", fecha.toString()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		boolean liberado = false;
		for (JsonNode slot : objectMapper.readTree(slotsLibresJson)) {
			if (fechaHora.equals(slot.get("fechaHora").asText())) {
				liberado = true;
			}
		}
		assertThat(liberado).isTrue();

		verify(emailService, atLeastOnce()).enviarHtml(eq("ana@example.com"), anyString(), anyString());
	}
}
