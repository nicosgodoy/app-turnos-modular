package com.nicosgodoy.backend_turnoApp.shared.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO",
				"Necesitás iniciar sesión para realizar esta acción.");
	}

	@Override
	public void handle(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		escribir(response, HttpServletResponse.SC_FORBIDDEN, "PROHIBIDO",
				"No tenés permiso para realizar esta acción.");
	}

	private void escribir(HttpServletResponse response, int estado, String codigo, String mensaje) throws IOException {
		response.setStatus(estado);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"codigo\":\"" + codigo + "\",\"mensaje\":\"" + mensaje + "\"}");
	}
}
