package com.nicosgodoy.backend_turnoApp.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.turnos")
public record TurnoProperties(int diasMaximos, int anticipacionMinimaHoras, int cancelacionMinimaHoras) {
}
