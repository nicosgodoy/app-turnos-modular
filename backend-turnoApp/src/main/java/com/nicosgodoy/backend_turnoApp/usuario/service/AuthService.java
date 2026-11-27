package com.nicosgodoy.backend_turnoApp.usuario.service;

import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.usuario.dto.LoginRequest;
import com.nicosgodoy.backend_turnoApp.usuario.dto.UsuarioResponse;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import com.nicosgodoy.backend_turnoApp.usuario.repository.UsuarioRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final UsuarioService usuarioService;
	private final TokenService tokenService;

	@Transactional(readOnly = true)
	public UsuarioResponse me(UUID usuarioId) {
		return usuarioService.me(usuarioId);
	}

	@Transactional
	public Tokens login(LoginRequest request) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
				.orElseThrow(() -> ApiException.noAutorizado("Email o contraseña incorrectos."));
		if (usuario.getRol() == Rol.PACIENTE) {
			throw ApiException.prohibido("Los pacientes ingresan con DNI y apellido.");
		}
		if (!usuario.isActivo()) {
			throw ApiException.prohibido("Tu usuario está deshabilitado.");
		}
		if (usuario.getPasswordHash() == null
				|| !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
			throw ApiException.noAutorizado("Email o contraseña incorrectos.");
		}
		return tokenService.generar(usuario);
	}

	@Transactional
	public Tokens refrescar(String refreshToken) {
		return tokenService.refrescar(refreshToken);
	}

	@Transactional
	public void cerrarSesion(String refreshToken) {
		tokenService.cerrarSesion(refreshToken);
	}
}
