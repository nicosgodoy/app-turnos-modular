package com.nicosgodoy.backend_turnoApp.turno.dto;

import com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoTurnoRequest(@NotNull EstadoTurno estado, @Size(max = 500) String motivo) {
}
