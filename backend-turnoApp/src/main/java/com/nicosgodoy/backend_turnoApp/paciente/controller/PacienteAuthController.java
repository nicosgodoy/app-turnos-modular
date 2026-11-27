package com.nicosgodoy.backend_turnoApp.paciente.controller;

import com.nicosgodoy.backend_turnoApp.paciente.dto.OtpRequest;
import com.nicosgodoy.backend_turnoApp.paciente.dto.OtpSolicitadoResponse;
import com.nicosgodoy.backend_turnoApp.paciente.dto.OtpVerifyRequest;
import com.nicosgodoy.backend_turnoApp.paciente.service.PacienteAuthService;
import com.nicosgodoy.backend_turnoApp.shared.security.RefreshCookieFactory;
import com.nicosgodoy.backend_turnoApp.usuario.dto.TokenResponse;
import com.nicosgodoy.backend_turnoApp.usuario.service.Tokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/paciente")
@RequiredArgsConstructor
public class PacienteAuthController {

	private final PacienteAuthService pacienteAuthService;
	private final RefreshCookieFactory cookieFactory;

	@PostMapping("/otp")
	public OtpSolicitadoResponse solicitarOtp(@Valid @RequestBody OtpRequest request) {
		return pacienteAuthService.solicitarOtp(request);
	}

	@PostMapping("/otp/verificar")
	public ResponseEntity<TokenResponse> verificarOtp(@Valid @RequestBody OtpVerifyRequest request,
			HttpServletRequest httpRequest) {
		return conCookie(pacienteAuthService.verificarOtp(request), httpRequest);
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
