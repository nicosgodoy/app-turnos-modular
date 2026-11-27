package com.nicosgodoy.backend_turnoApp.cobertura.dto;

import java.util.UUID;

public record PlanResponse(UUID id, String nombre, String codigo, boolean activo) {
}
