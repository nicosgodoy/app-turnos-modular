package com.nicosgodoy.backend_turnoApp.shared.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

	@Bean
	public ZoneId zonaHoraria(ClinicaProperties propiedades) {
		return ZoneId.of(propiedades.zonaHoraria());
	}

	@Bean
	public Clock clock(ZoneId zonaHoraria) {
		return Clock.system(zonaHoraria);
	}
}
