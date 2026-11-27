package com.nicosgodoy.backend_turnoApp.cobertura.repository;

import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObraSocialRepository extends JpaRepository<ObraSocial, UUID> {

	List<ObraSocial> findAllByActivoTrueOrderByNombreAsc();

	List<ObraSocial> findAllByOrderByNombreAsc();

	Optional<ObraSocial> findByCodigoIgnoreCase(String codigo);
}
