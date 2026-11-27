package com.nicosgodoy.backend_turnoApp.usuario.controller;

import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.shared.security.RefreshCookieFactory;
import com.nicosgodoy.backend_turnoApp.shared.security.SecurityUtils;
import com.nicosgodoy.backend_turnoApp.usuario.dto.LoginRequest;
import com.nicosgodoy.backend_turnoApp.usuario.dto.MensajeResponse;
import com.nicosgodoy.backend_turnoApp.usuario.dto.TokenResponse;
import com.nicosgodoy.backend_turnoApp.usuario.dto.UsuarioResponse;
import com.nicosgodoy.backend_turnoApp.usuario.service.AuthService;
import com.nicosgodoy.backend_turnoApp.usuario.service.Tokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final RefreshCookieFactory cookieFactory;

	@PostMapping("/staff/login")
	public ResponseEntity<TokenResponse> loginStaff(@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		return conCookie(authService.login(request), httpRequest);
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refrescar(
			@CookieValue(name = RefreshCookieFactory.COOKIE_REFRESH, required = false) String refreshToken,
			HttpServletRequest httpRequest) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw ApiException.noAutorizado("La sesión expiró. Volvé a ingresar.");
		}
		return conCookie(authService.refrescar(refreshToken), httpRequest);
	}

	@PostMapping("/logout")
	public ResponseEntity<MensajeResponse> cerrarSesion(
			@CookieValue(name = RefreshCookieFactory.COOKIE_REFRESH, required = false) String refreshToken,
			HttpServletRequest httpRequest) {
		authService.cerrarSesion(refreshToken);
		ResponseCookie cookie = cookieFactory.limpiar(httpRequest.isSecure());
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(new MensajeResponse("Sesión cerrada."));
	}

	@GetMapping("/me")
	public UsuarioResponse me() {
		return authService.me(SecurityUtils.actual().id());
	}

	private ResponseEntity<TokenResponse> conCookie(Tokens tokens, HttpServletRequest httpRequest) {
		ResponseCookie cookie = cookieFactory.crear(tokens.refreshToken(), httpRequest.isSecure());
		TokenResponse respuesta = new TokenResponse(tokens.accessToken(), "Bearer", tokens.expiresIn(),
				tokens.usuario());
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(respuesta);
	}
}
