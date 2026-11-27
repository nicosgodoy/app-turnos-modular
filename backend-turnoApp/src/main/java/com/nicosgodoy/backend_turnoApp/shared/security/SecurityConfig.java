package com.nicosgodoy.backend_turnoApp.shared.security;

import com.nicosgodoy.backend_turnoApp.shared.config.CorsProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
			SecurityErrorHandler errorHandler) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(autorizacion -> autorizacion
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(
								"/auth/**",
								"/especialidades",
								"/obras-sociales",
								"/medicos/**",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/error")
						.permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/medico/**").hasRole("MEDICO")
						.anyRequest().authenticated())
				.exceptionHandling(excepciones -> excepciones
						.authenticationEntryPoint(errorHandler)
						.accessDeniedHandler(errorHandler))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(CorsProperties propiedades) {
		CorsConfiguration configuracion = new CorsConfiguration();
		configuracion.setAllowedOrigins(propiedades.allowedOrigins());
		configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuracion.setAllowedHeaders(List.of("*"));
		configuracion.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
		fuente.registerCorsConfiguration("/**", configuracion);
		return fuente;
	}
}
