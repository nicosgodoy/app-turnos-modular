package com.nicosgodoy.backend_turnoApp.usuario.repository;

import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);
}
