package com.nicosgodoy.backend_turnoApp.usuario.service;

import com.nicosgodoy.backend_turnoApp.usuario.dto.UsuarioResponse;

public record Tokens(String accessToken, String refreshToken, long expiresIn, UsuarioResponse usuario) {
}
