package com.nicosgodoy.backend_turnoApp.agenda.dto;

import java.util.UUID;

public record ConsultorioResponse(UUID id, String nombre, String ubicacion, boolean activo) {
}
