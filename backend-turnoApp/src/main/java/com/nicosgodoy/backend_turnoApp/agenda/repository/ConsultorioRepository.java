package com.nicosgodoy.backend_turnoApp.agenda.repository;

import com.nicosgodoy.backend_turnoApp.agenda.entity.Consultorio;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorioRepository extends JpaRepository<Consultorio, UUID> {

	List<Consultorio> findAllByActivoTrueOrderByNombreAsc();

	List<Consultorio> findAllByOrderByNombreAsc();

	Optional<Consultorio> findByNombreIgnoreCase(String nombre);
}
