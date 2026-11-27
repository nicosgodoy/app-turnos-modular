package com.nicosgodoy.backend_turnoApp.cobertura.repository;

import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

	List<Plan> findByObraSocialIdAndActivoTrueOrderByNombreAsc(UUID obraSocialId);

	List<Plan> findByObraSocialIdOrderByNombreAsc(UUID obraSocialId);

	Optional<Plan> findByIdAndObraSocialId(UUID id, UUID obraSocialId);

	Optional<Plan> findByObraSocialIdAndCodigoIgnoreCase(UUID obraSocialId, String codigo);
}
