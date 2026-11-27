package com.nicosgodoy.backend_turnoApp.agenda.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SlotResponse(OffsetDateTime fechaHora, UUID consultorioId, String consultorio, int duracionMin) {
}
