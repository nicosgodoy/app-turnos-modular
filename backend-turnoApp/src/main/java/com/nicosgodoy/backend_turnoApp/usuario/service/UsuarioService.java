package com.nicosgodoy.backend_turnoApp.usuario.service;

import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.usuario.dto.UsuarioResponse;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import com.nicosgodoy.backend_turnoApp.usuario.repository.UsuarioRepository;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public Usuario buscar(UUID id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el usuario indicado."));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse me(UUID id) {
		return UsuarioResponse.de(buscar(id));
	}

	@Transactional
	public Usuario crearPaciente(String nombre, String apellido, String email) {
		String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
			throw ApiException.conflicto(
					"Ya existe un usuario con ese email. Contactá a la clínica para resolverlo.");
		}
		Usuario usuario = new Usuario();
		usuario.setEmail(emailNormalizado);
		usuario.setNombre(nombre.trim());
		usuario.setApellido(apellido.trim());
		usuario.setRol(Rol.PACIENTE);
		usuario.setActivo(false);
		return usuarioRepository.save(usuario);
	}

	@Transactional
	public Usuario crearStaff(String nombre, String apellido, String email, String password, Rol rol) {
		String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
			throw ApiException.conflicto("Ya existe un usuario con ese email.");
		}
		Usuario usuario = new Usuario();
		usuario.setEmail(emailNormalizado);
		usuario.setNombre(nombre.trim());
		usuario.setApellido(apellido.trim());
		usuario.setRol(rol);
		usuario.setActivo(true);
		usuario.setPasswordHash(passwordEncoder.encode(password));
		return usuarioRepository.save(usuario);
	}
}
