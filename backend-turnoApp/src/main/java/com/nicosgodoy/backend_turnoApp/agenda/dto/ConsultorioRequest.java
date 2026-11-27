package com.nicosgodoy.backend_turnoApp.agenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultorioRequest(@NotBlank @Size(max = 120) String nombre, @Size(max = 200) String ubicacion) {
}
