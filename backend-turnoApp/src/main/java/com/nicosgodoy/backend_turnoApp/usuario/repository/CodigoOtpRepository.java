package com.nicosgodoy.backend_turnoApp.usuario.repository;

import com.nicosgodoy.backend_turnoApp.usuario.entity.CodigoOtp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodigoOtpRepository extends JpaRepository<CodigoOtp, UUID> {

	Optional<CodigoOtp> findFirstByUsuario_IdAndUsadoFalseOrderByCreadoEnDesc(UUID usuarioId);

	long countByUsuario_IdAndCreadoEnAfter(UUID usuarioId, Instant desde);

	@Modifying
	@Query("update CodigoOtp c set c.usado = true where c.usuario.id = :usuarioId and c.usado = false")
	int invalidarTodos(@Param("usuarioId") UUID usuarioId);
}
