package com.nicosgodoy.backend_turnoApp.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notificaciones")
public record NotificacionProperties(String remitente) {
}
