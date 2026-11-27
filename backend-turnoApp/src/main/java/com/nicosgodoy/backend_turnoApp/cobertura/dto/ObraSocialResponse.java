package com.nicosgodoy.backend_turnoApp.cobertura.dto;

import java.util.List;
import java.util.UUID;

public record ObraSocialResponse(UUID id, String nombre, String codigo, boolean activo, List<PlanResponse> planes) {
}
