package com.nicosgodoy.backend_turnoApp.medico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CrearMedicoRequest(
		@NotBlank @Size(max = 120) String nombre,
		@NotBlank @Size(max = 120) String apellido,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotBlank @Size(max = 40) String matricula,
		@Min(5) @Max(240) int duracionTurnoMin,
		@DecimalMin("0") BigDecimal precioParticular,
		@Size(max = 2000) String biografia,
		@NotEmpty List<UUID> especialidadIds,
		List<CoberturaRequest> coberturas) {
}
