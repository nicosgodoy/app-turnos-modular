package com.nicosgodoy.backend_turnoApp.shared.security;

import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static UsuarioPrincipal actual() {
		Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {
			throw ApiException.noAutorizado("Necesitás iniciar sesión.");
		}
		return principal;
	}
}
