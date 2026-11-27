package com.nicosgodoy.backend_turnoApp.notificacion.service;

import com.nicosgodoy.backend_turnoApp.notificacion.entity.EstadoNotificacion;
import com.nicosgodoy.backend_turnoApp.notificacion.entity.Notificacion;
import com.nicosgodoy.backend_turnoApp.notificacion.entity.TipoNotificacion;
import com.nicosgodoy.backend_turnoApp.notificacion.repository.NotificacionRepository;
import com.nicosgodoy.backend_turnoApp.shared.config.ClinicaProperties;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

	private static final Locale ESPANOL = Locale.forLanguageTag("es-AR");

	private final NotificacionRepository notificacionRepository;
	private final EmailService emailService;
	private final ClinicaProperties clinicaProperties;
	private final ZoneId zonaHoraria;

	@Transactional
	public void enviarConfirmacion(DatosTurnoNotificacion datos) {
		enviar(TipoNotificacion.CONFIRMACION, datos, "Turno confirmado",
				cuerpo("Tu turno está confirmado", datos,
						"Reservamos tu turno. Te esperamos con tu DNI y credencial de la obra social."));
	}

	@Transactional
	public void enviarRecordatorio(DatosTurnoNotificacion datos) {
		enviar(TipoNotificacion.RECORDATORIO, datos, "Recordatorio de turno",
				cuerpo("Mañana tenés turno", datos,
						"Este es un recordatorio de tu turno. Si no podés asistir, cancelalo desde la web."));
	}

	@Transactional
	public void enviarCancelacion(DatosTurnoNotificacion datos) {
		enviar(TipoNotificacion.CANCELACION, datos, "Turno cancelado",
				cuerpo("Tu turno fue cancelado", datos,
						"El turno quedó cancelado. Podés reservar uno nuevo cuando quieras."));
	}

	@Transactional
	public void enviarReprogramacion(DatosTurnoNotificacion datos) {
		enviar(TipoNotificacion.REPROGRAMACION, datos, "Turno reprogramado",
				cuerpo("Tu turno fue reprogramado", datos,
						"Estos son los nuevos datos de tu turno."));
	}

	@Transactional(readOnly = true)
	public boolean existeRecordatorioEnviado(UUID turnoId) {
		return notificacionRepository.existsByTurnoIdAndTipoAndEstado(turnoId,
				TipoNotificacion.RECORDATORIO, EstadoNotificacion.ENVIADA);
	}

	private void enviar(TipoNotificacion tipo, DatosTurnoNotificacion datos, String asunto, String html) {
		Notificacion notificacion = new Notificacion();
		notificacion.setTurnoId(datos.turnoId());
		notificacion.setTipo(tipo);
		notificacion.setDestinatario(datos.pacienteEmail());
		notificacion.setEstado(EstadoNotificacion.PENDIENTE);
		notificacionRepository.save(notificacion);

		try {
			emailService.enviarHtml(datos.pacienteEmail(), asunto, html);
			notificacion.setEstado(EstadoNotificacion.ENVIADA);
			notificacion.setEnviadoEn(Instant.now());
		} catch (RuntimeException ex) {
			log.error("No se pudo enviar la notificación {} del turno {}", tipo, datos.turnoId(), ex);
			notificacion.setEstado(EstadoNotificacion.ERROR);
			notificacion.setError(truncar(ex.getMessage()));
		}
		notificacionRepository.save(notificacion);
	}

	private String cuerpo(String titulo, DatosTurnoNotificacion datos, String introduccion) {
		String fecha = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", ESPANOL)
				.withZone(zonaHoraria)
				.format(datos.fechaHora());
		fecha = fecha.substring(0, 1).toUpperCase(ESPANOL) + fecha.substring(1);
		String motivo = datos.motivo() == null || datos.motivo().isBlank()
				? ""
				: "<p><strong>Motivo:</strong> " + datos.motivo() + "</p>";
		return """
				<div style="font-family: Arial, sans-serif; max-width: 560px; margin: auto; color: #0f172a;">
				  <h2 style="color: #0f766e;">%s</h2>
				  <p>Hola %s,</p>
				  <p>%s</p>
				  <ul>
				    <li><strong>Profesional:</strong> %s</li>
				    <li><strong>Fecha:</strong> %s</li>
				    <li><strong>Consultorio:</strong> %s</li>
				  </ul>
				  %s
				  <hr>
				  <p style="font-size: 12px; color: #64748b;">%s — %s</p>
				</div>
				""".formatted(titulo, datos.pacienteNombre(), introduccion, datos.medicoNombre(), fecha,
				datos.consultorio(), motivo, clinicaProperties.nombre(), clinicaProperties.direccion());
	}

	private String truncar(String mensaje) {
		if (mensaje == null) {
			return null;
		}
		return mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje;
	}
}
