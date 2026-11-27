package com.nicosgodoy.backend_turnoApp.shared.security;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshCookieFactory {

	public static final String COOKIE_REFRESH = "refresh_token";

	private final JwtService jwtService;

	public ResponseCookie crear(String valor, boolean seguro) {
		return ResponseCookie.from(COOKIE_REFRESH, valor)
				.httpOnly(true)
				.secure(seguro)
				.sameSite("Strict")
				.path("/auth")
				.maxAge(Duration.ofDays(jwtService.diasRefreshToken()))
				.build();
	}

	public ResponseCookie limpiar(boolean seguro) {
		return ResponseCookie.from(COOKIE_REFRESH, "")
				.httpOnly(true)
				.secure(seguro)
				.sameSite("Strict")
				.path("/auth")
				.maxAge(Duration.ZERO)
				.build();
	}
}
