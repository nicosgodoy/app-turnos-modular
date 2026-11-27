package com.nicosgodoy.backend_turnoApp.turno.entity;

public enum EstadoTurno {
	PENDIENTE,
	CONFIRMADO,
	ATENDIDO,
	CANCELADO_PACIENTE,
	CANCELADO_CLINICA,
	AUSENTE;

	public boolean esCancelable() {
		return this == PENDIENTE || this == CONFIRMADO;
	}
}
