package com.nicosgodoy.backend_turnoApp.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.clinica")
public record ClinicaProperties(String nombre, String direccion, String zonaHoraria) {
}
