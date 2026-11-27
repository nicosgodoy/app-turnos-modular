package com.nicosgodoy.backend_turnoApp.shared.security;

import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import java.util.UUID;

public record UsuarioPrincipal(UUID id, String email, String nombre, Rol rol) {
}
