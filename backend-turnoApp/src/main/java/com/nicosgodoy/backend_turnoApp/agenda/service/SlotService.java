package com.nicosgodoy.backend_turnoApp.agenda.service;

import com.nicosgodoy.backend_turnoApp.agenda.dto.SlotResponse;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SlotService {

	private final DisponibilidadRepository disponibilidadRepository;
	private final ExcepcionAgendaRepository excepcionAgendaRepository;
	private final MedicoService medicoService;
	private final ReservasPort reservasPort;
	private final TurnoProperties turnoProperties;
	private final Clock clock;
	private final ZoneId zonaHoraria;

	@Transactional(readOnly = true)
	public List<SlotResponse> slots(UUID medicoId, LocalDate fecha) {
		Medico medico = medicoService.obtenerActivo(medicoId);
		LocalDate hoy = LocalDate.now(clock);
		if (fecha.isBefore(hoy)) {
			return List.of();
		}
		if (fecha.isAfter(hoy.plusDays(turnoProperties.diasMaximos()))) {
			throw ApiException.solicitudInvalida(
					"Solo se pueden reservar turnos con hasta " + turnoProperties.diasMaximos() + " días de anticipación.");
		}

		List<Disponibilidad> bloques = disponibilidadRepository
				.buscarVigentes(medicoId, fecha.getDayOfWeek().getValue(), fecha);
		if (bloques.isEmpty()) {
			return List.of();
		}

		List<ExcepcionAgenda> excepciones = excepcionAgendaRepository.buscarDelDia(medicoId, fecha);
		Set<LocalDateTime> ocupados = reservasPort.fechasOcupadas(medicoId, fecha, fecha);
		LocalDateTime minimo = LocalDateTime.now(clock)
				.plusHours(turnoProperties.anticipacionMinimaHoras());

		Map<LocalDateTime, SlotResponse> resultado = new TreeMap<>();
		for (Disponibilidad bloque : bloques) {
			int duracion = medico.getDuracionTurnoMin();
			LocalTime hora = bloque.getHoraDesde();
			while (!hora.plusMinutes(duracion).isAfter(bloque.getHoraHasta())) {
				LocalDateTime inicio = LocalDateTime.of(fecha, hora);
				LocalDateTime fin = inicio.plusMinutes(duracion);
				boolean disponible = !inicio.isBefore(minimo)
						&& !ocupados.contains(inicio)
						&& !bloqueado(excepciones, fecha, inicio, fin);
				if (disponible) {
					resultado.putIfAbsent(inicio, new SlotResponse(
							inicio.atZone(zonaHoraria).toOffsetDateTime(),
							bloque.getConsultorio().getId(),
							bloque.getConsultorio().getNombre(),
							duracion));
				}
				hora = hora.plusMinutes(duracion);
			}
		}
		return List.copyOf(resultado.values());
	}

	@Transactional(readOnly = true)
	public SlotResponse validarSlot(UUID medicoId, OffsetDateTime fechaHora) {
		LocalDate fecha = fechaHora.atZoneSameInstant(zonaHoraria).toLocalDate();
		return slots(medicoId, fecha).stream()
				.filter(slot -> slot.fechaHora().isEqual(fechaHora))
				.findFirst()
				.orElseThrow(() -> ApiException.conflicto(
						"El horario seleccionado ya no está disponible. Elegí otro."));
	}

	private boolean bloqueado(List<ExcepcionAgenda> excepciones, LocalDate fecha,
			LocalDateTime inicio, LocalDateTime fin) {
		for (ExcepcionAgenda excepcion : excepciones) {
			if (excepcion.isTodoElDia()) {
				return true;
			}
			LocalDateTime desde = LocalDateTime.of(fecha, excepcion.getHoraDesde());
			LocalDateTime hasta = LocalDateTime.of(fecha, excepcion.getHoraHasta());
			if (inicio.isBefore(hasta) && fin.isAfter(desde)) {
				return true;
			}
		}
		return false;
	}
}
