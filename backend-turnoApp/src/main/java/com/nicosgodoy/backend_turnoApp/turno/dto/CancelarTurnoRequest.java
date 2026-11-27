package com.nicosgodoy.backend_turnoApp.turno.dto;

import jakarta.validation.constraints.Size;

public record CancelarTurnoRequest(@Size(max = 500) String motivo) {
}
