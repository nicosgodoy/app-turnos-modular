package com.nicosgodoy.backend_turnoApp.turno.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record ReprogramarTurnoRequest(@NotNull OffsetDateTime fechaHora) {
}
