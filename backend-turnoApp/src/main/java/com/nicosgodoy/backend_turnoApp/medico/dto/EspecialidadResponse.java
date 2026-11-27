package com.nicosgodoy.backend_turnoApp.medico.dto;

import java.util.UUID;

public record EspecialidadResponse(UUID id, String nombre, int duracionDefaultMin, boolean activo) {
}
