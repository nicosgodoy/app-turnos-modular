package com.nicosgodoy.backend_turnoApp.usuario.dto;

import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import java.util.UUID;

public record UsuarioResponse(UUID id, String email, String nombre, String apellido, Rol rol) {

	public static UsuarioResponse de(Usuario usuario) {
		return new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getNombre(),
				usuario.getApellido(), usuario.getRol());
	}
}
