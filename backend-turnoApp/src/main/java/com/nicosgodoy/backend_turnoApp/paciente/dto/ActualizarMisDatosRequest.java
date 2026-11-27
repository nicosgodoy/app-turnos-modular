package com.nicosgodoy.backend_turnoApp.paciente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ActualizarMisDatosRequest(
		@NotBlank @Size(max = 30) String telefono,
		UUID obraSocialId,
		UUID planId,
		@Size(max = 50) String numeroAfiliado) {
}
