package com.nicosgodoy.backend_turnoApp.notificacion.service;

import java.time.Instant;

public interface EmailService {

	void enviarCodigoOtp(String destinatario, String nombre, String codigo, Instant expiraEn);

	void enviarHtml(String destinatario, String asunto, String html);
}
