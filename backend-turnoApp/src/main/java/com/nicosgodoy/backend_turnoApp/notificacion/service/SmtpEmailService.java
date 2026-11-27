package com.nicosgodoy.backend_turnoApp.notificacion.service;

import com.nicosgodoy.backend_turnoApp.shared.config.ClinicaProperties;
import com.nicosgodoy.backend_turnoApp.shared.config.NotificacionProperties;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

	private final JavaMailSender mailSender;
	private final NotificacionProperties notificacionProperties;
	private final ClinicaProperties clinicaProperties;
	private final ZoneId zonaHoraria;

	@Override
	public void enviarCodigoOtp(String destinatario, String nombre, String codigo, Instant expiraEn) {
		String vencimiento = DateTimeFormatter.ofPattern("HH:mm")
				.withZone(zonaHoraria)
				.format(expiraEn);
		String html = """
				<div style="font-family: Arial, sans-serif; max-width: 560px; margin: auto;">
				  <h2 style="color: #0f766e;">%s</h2>
				  <p>Hola %s,</p>
				  <p>Tu código para ingresar es:</p>
				  <p style="font-size: 32px; letter-spacing: 8px; font-weight: bold; color: #0f766e;">%s</p>
				  <p>Vence hoy a las %s. Si no solicitaste este código, podés ignorar este mensaje.</p>
				  <hr>
				  <p style="font-size: 12px; color: #64748b;">%s — %s</p>
				</div>
				""".formatted(clinicaProperties.nombre(), nombre, codigo, vencimiento,
				clinicaProperties.nombre(), clinicaProperties.direccion());
		enviarHtml(destinatario, "Tu código de acceso", html);
	}

	@Override
	public void enviarHtml(String destinatario, String asunto, String html) {
		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
			helper.setFrom(notificacionProperties.remitente());
			helper.setTo(destinatario);
			helper.setSubject(asunto);
			helper.setText(html, true);
			mailSender.send(mensaje);
		} catch (Exception ex) {
			throw new IllegalStateException("No se pudo enviar el email a " + destinatario, ex);
		}
	}
}
