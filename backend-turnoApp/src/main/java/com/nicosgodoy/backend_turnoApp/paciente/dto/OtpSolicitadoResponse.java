package com.nicosgodoy.backend_turnoApp.paciente.dto;

public record OtpSolicitadoResponse(String mensaje, int expiraEnMinutos, String emailEnmascarado) {
}
