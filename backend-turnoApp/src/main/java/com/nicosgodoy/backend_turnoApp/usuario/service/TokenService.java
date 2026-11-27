package com.nicosgodoy.backend_turnoApp.usuario.service;

import com.nicosgodoy.backend_turnoApp.shared.config.JwtProperties;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.shared.security.JwtService;
import com.nicosgodoy.backend_turnoApp.shared.security.UsuarioPrincipal;
import com.nicosgodoy.backend_turnoApp.usuario.dto.UsuarioResponse;
import com.nicosgodoy.backend_turnoApp.usuario.entity.RefreshToken;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import com.nicosgodoy.backend_turnoApp.usuario.repository.RefreshTokenRepository;
import com.nicosgodoy.backend_turnoApp.usuario.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final Clock clock;

	@Transactional
	public Tokens generar(Usuario usuario) {
		UsuarioPrincipal principal = new UsuarioPrincipal(usuario.getId(), usuario.getEmail(),
				usuario.nombreCompleto(), usuario.getRol());
		String accessToken = jwtService.generarAccessToken(principal);
		String refreshToken = jwtService.generarRefreshToken(usuario.getId());
		refreshTokenRepository.save(new RefreshToken(usuario, hash(refreshToken),
				clock.instant().plus(Duration.ofDays(jwtProperties.refreshDays()))));
		return new Tokens(accessToken, refreshToken, jwtService.minutosAccessToken() * 60L,
				UsuarioResponse.de(usuario));
	}

	@Transactional
	public Tokens refrescar(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw ApiException.noAutorizado("La sesión expiró. Volvé a ingresar.");
		}
		Claims claims;
		try {
			claims = jwtService.parsear(refreshToken);
		} catch (JwtException | IllegalArgumentException ex) {
			throw ApiException.noAutorizado("La sesión expiró. Volvé a ingresar.");
		}
		if (!"refresh".equals(claims.get("tipo", String.class))) {
			throw ApiException.noAutorizado("La sesión expiró. Volvé a ingresar.");
		}

		RefreshToken guardado = refreshTokenRepository.findByTokenHashAndRevocadoFalse(hash(refreshToken))
				.orElseThrow(() -> ApiException.noAutorizado("La sesión expiró. Volvé a ingresar."));
		if (guardado.getExpiraEn().isBefore(clock.instant())) {
			throw ApiException.noAutorizado("La sesión expiró. Volvé a ingresar.");
		}

		Usuario usuario = usuarioRepository.findById(UUID.fromString(claims.getSubject()))
				.orElseThrow(() -> ApiException.noAutorizado("La sesión expiró. Volvé a ingresar."));
		if (!usuario.isActivo()) {
			throw ApiException.prohibido("Tu usuario está deshabilitado.");
		}
		guardado.setRevocado(true);
		return generar(usuario);
	}

	@Transactional
	public void cerrarSesion(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			return;
		}
		refreshTokenRepository.findByTokenHashAndRevocadoFalse(hash(refreshToken))
				.ifPresent(token -> token.setRevocado(true));
	}

	private String hash(String valor) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 no disponible", ex);
		}
	}
}
