package com.nicosgodoy.backend_turnoApp.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(int expirationMinutes, int maxSends, int sendWindowMinutes, int maxAttempts,
		boolean logCodigo) {
}
