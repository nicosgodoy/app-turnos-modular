package com.nicosgodoy.backend_turnoApp.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nicosgodoy.backend_turnoApp.agenda.dto.SlotResponse;
import com.nicosgodoy.backend_turnoApp.agenda.entity.Consultorio;
import com.nicosgodoy.backend_turnoApp.agenda.entity.Disponibilidad;
import com.nicosgodoy.backend_turnoApp.agenda.entity.ExcepcionAgenda;
import com.nicosgodoy.backend_turnoApp.agenda.repository.DisponibilidadRepository;
import com.nicosgodoy.backend_turnoApp.agenda.repository.ExcepcionAgendaRepository;
import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import com.nicosgodoy.backend_turnoApp.shared.config.TurnoProperties;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

	private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
	private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);

	@Mock
	private DisponibilidadRepository disponibilidadRepository;
	@Mock
	private ExcepcionAgendaRepository excepcionAgendaRepository;
	@Mock
	private MedicoService medicoService;
	@Mock
	private ReservasPort reservasPort;

	private SlotService slotService;
	private Medico medico;
	private Consultorio consultorio;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(LocalDateTime.of(LUNES, LocalTime.of(7, 0)).atZone(ZONA).toInstant(), ZONA);
		slotService = new SlotService(disponibilidadRepository, excepcionAgendaRepository, medicoService,
				reservasPort, new TurnoProperties(90, 2, 24), clock, ZONA);

		medico = new Medico();
		medico.setId(UUID.randomUUID());
		medico.setDuracionTurnoMin(30);
		medico.setActivo(true);

		consultorio = new Consultorio();
		consultorio.setId(UUID.randomUUID());
		consultorio.setNombre("Consultorio 1");
	}

	@Test
	void generaSlotsLibresYDescartaLosReservados() {
		when(medicoService.obtenerActivo(medico.getId())).thenReturn(medico);
		when(disponibilidadRepository.buscarVigentes(medico.getId(), 1, LUNES))
				.thenReturn(List.of(bloque(9, 0, 11, 0)));
		when(excepcionAgendaRepository.buscarDelDia(medico.getId(), LUNES)).thenReturn(List.of());
		when(reservasPort.fechasOcupadas(medico.getId(), LUNES, LUNES))
				.thenReturn(Set.of(LocalDateTime.of(LUNES, LocalTime.of(10, 0))));

		List<SlotResponse> slots = slotService.slots(medico.getId(), LUNES);

		assertThat(slots).extracting(slot -> slot.fechaHora().toLocalTime())
				.containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 30));
	}

	@Test
	void descartaSlotsQueSolapanConUnaExcepcionParcial() {
		ExcepcionAgenda excepcion = new ExcepcionAgenda();
		excepcion.setTodoElDia(false);
		excepcion.setHoraDesde(LocalTime.of(9, 30));
		excepcion.setHoraHasta(LocalTime.of(10, 0));

		when(medicoService.obtenerActivo(medico.getId())).thenReturn(medico);
		when(disponibilidadRepository.buscarVigentes(medico.getId(), 1, LUNES))
				.thenReturn(List.of(bloque(9, 0, 11, 0)));
		when(excepcionAgendaRepository.buscarDelDia(medico.getId(), LUNES)).thenReturn(List.of(excepcion));
		when(reservasPort.fechasOcupadas(medico.getId(), LUNES, LUNES)).thenReturn(Set.of());

		List<SlotResponse> slots = slotService.slots(medico.getId(), LUNES);

		assertThat(slots).extracting(slot -> slot.fechaHora().toLocalTime())
				.containsExactly(LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(10, 30));
	}

	@Test
	void rechazaFechasFueraDeLaVentanaDeReserva() {
		when(medicoService.obtenerActivo(medico.getId())).thenReturn(medico);

		assertThatThrownBy(() -> slotService.slots(medico.getId(), LUNES.plusDays(120)))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("90 días");
	}

	@Test
	void validarSlotRechazaUnHorarioQueNoEstaDisponible() {
		when(medicoService.obtenerActivo(medico.getId())).thenReturn(medico);
		when(disponibilidadRepository.buscarVigentes(medico.getId(), 1, LUNES))
				.thenReturn(List.of(bloque(9, 0, 10, 0)));
		when(excepcionAgendaRepository.buscarDelDia(medico.getId(), LUNES)).thenReturn(List.of());
		when(reservasPort.fechasOcupadas(medico.getId(), LUNES, LUNES)).thenReturn(Set.of());

		var horarioInvalido = LocalDateTime.of(LUNES, LocalTime.of(11, 0)).atZone(ZONA).toOffsetDateTime();

		assertThatThrownBy(() -> slotService.validarSlot(medico.getId(), horarioInvalido))
				.isInstanceOf(ApiException.class)
				.hasMessageContaining("ya no está disponible");
	}

	private Disponibilidad bloque(int horaDesde, int minutoDesde, int horaHasta, int minutoHasta) {
		Disponibilidad disponibilidad = new Disponibilidad();
		disponibilidad.setMedico(medico);
		disponibilidad.setConsultorio(consultorio);
		disponibilidad.setDiaSemana(1);
		disponibilidad.setHoraDesde(LocalTime.of(horaDesde, minutoDesde));
		disponibilidad.setHoraHasta(LocalTime.of(horaHasta, minutoHasta));
		disponibilidad.setVigenteDesde(LUNES.minusDays(30));
		disponibilidad.setActivo(true);
		return disponibilidad;
	}
}
