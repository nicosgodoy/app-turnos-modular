package com.nicosgodoy.backend_turnoApp.usuario.service;

import com.nicosgodoy.backend_turnoApp.shared.config.AdminProperties;
import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import com.nicosgodoy.backend_turnoApp.usuario.repository.UsuarioRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

	private final AdminProperties adminProperties;
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(ApplicationArguments args) {
		if (adminProperties.password() == null || adminProperties.password().isBlank()) {
			log.info("No se crea el administrador inicial: falta APP_ADMIN_PASSWORD.");
			return;
		}
		String email = adminProperties.email().trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			return;
		}
		Usuario admin = new Usuario();
		admin.setEmail(email);
		admin.setNombre("Administrador");
		admin.setApellido("General");
		admin.setRol(Rol.ADMIN);
		admin.setActivo(true);
		admin.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
		usuarioRepository.save(admin);
		log.info("Administrador inicial creado: {}", email);
	}
}
