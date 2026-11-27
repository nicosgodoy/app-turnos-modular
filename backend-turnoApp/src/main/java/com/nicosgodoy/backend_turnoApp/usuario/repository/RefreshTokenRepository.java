package com.nicosgodoy.backend_turnoApp.usuario.repository;

import com.nicosgodoy.backend_turnoApp.usuario.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHashAndRevocadoFalse(String tokenHash);

	@Modifying
	@Query("update RefreshToken r set r.revocado = true where r.usuario.id = :usuarioId and r.revocado = false")
	int revocarTodos(@Param("usuarioId") UUID usuarioId);
}
