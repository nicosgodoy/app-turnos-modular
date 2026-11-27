package com.nicosgodoy.backend_turnoApp.usuario.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, UsuarioResponse usuario) {
}
