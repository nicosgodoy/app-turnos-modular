package com.nicosgodoy.backend_turnoApp.turno.dto;

import com.nicosgodoy.backend_turnoApp.turno.entity.TipoCobertura;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservarTurnoRequest(
		@NotNull UUID medicoId,
		@NotNull OffsetDateTime fechaHora,
		TipoCobertura tipoCobertura,
		@Size(max = 500) String motivo) {
}
