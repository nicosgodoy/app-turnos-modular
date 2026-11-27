package com.nicosgodoy.backend_turnoApp.turno.service;

import com.nicosgodoy.backend_turnoApp.notificacion.service.NotificacionService;
import com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno;
import com.nicosgodoy.backend_turnoApp.turno.entity.Turno;
import com.nicosgodoy.backend_turnoApp.turno.repository.TurnoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioJob {

	private final TurnoRepository turnoRepository;
	private final TurnoService turnoService;
	private final NotificacionService notificacionService;
	private final Clock clock;
	private final ZoneId zonaHoraria;

	@Scheduled(cron = "0 0 9 * * *", zone = "${app.clinica.zona-horaria}")
	@Transactional
	public void enviarRecordatorios() {
		LocalDate manana = LocalDate.now(clock).plusDays(1);
		var desde = manana.atStartOfDay(zonaHoraria).toOffsetDateTime();
		var hasta = manana.plusDays(1).atStartOfDay(zonaHoraria).toOffsetDateTime();
		List<Turno> turnos = turnoRepository.findByEstadoInAndFechaHoraBetweenOrderByFechaHoraAsc(
				List.of(EstadoTurno.CONFIRMADO, EstadoTurno.PENDIENTE), desde, hasta);

		for (Turno turno : turnos) {
			if (notificacionService.existeRecordatorioEnviado(turno.getId())) {
				continue;
			}
			try {
				notificacionService.enviarRecordatorio(turnoService.datosParaNotificacion(turno));
			} catch (RuntimeException ex) {
				log.error("No se pudo enviar el recordatorio del turno {}", turno.getId(), ex);
			}
		}
	}
}
