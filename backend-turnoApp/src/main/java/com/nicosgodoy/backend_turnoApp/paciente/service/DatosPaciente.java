package com.nicosgodoy.backend_turnoApp.paciente.service;

import java.time.LocalDate;
import java.util.UUID;

public record DatosPaciente(
		String nombre,
		String apellido,
		String dni,
		LocalDate fechaNacimiento,
		String telefono,
		String email,
		UUID obraSocialId,
		UUID planId,
		String numeroAfiliado) {
}
