package com.nicosgodoy.backend_turnoApp.turno.service;

import com.nicosgodoy.backend_turnoApp.agenda.service.ReservasPort;
import com.nicosgodoy.backend_turnoApp.turno.repository.TurnoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservasAdapter implements ReservasPort {

	private final TurnoRepository turnoRepository;
	private final ZoneId zonaHoraria;

	@Override
	@Transactional(readOnly = true)
	public Set<LocalDateTime> fechasOcupadas(UUID medicoId, LocalDate desde, LocalDate hasta) {
		var inicio = desde.atStartOfDay(zonaHoraria).toOffsetDateTime();
		var fin = hasta.plusDays(1).atStartOfDay(zonaHoraria).toOffsetDateTime();
		return turnoRepository.fechasOcupadas(medicoId, inicio, fin).stream()
				.map(fecha -> fecha.atZoneSameInstant(zonaHoraria).toLocalDateTime())
				.collect(Collectors.toSet());
	}
}
