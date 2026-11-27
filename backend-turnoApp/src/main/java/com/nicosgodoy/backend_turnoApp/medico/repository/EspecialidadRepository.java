package com.nicosgodoy.backend_turnoApp.medico.repository;

import com.nicosgodoy.backend_turnoApp.medico.entity.Especialidad;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {

	List<Especialidad> findAllByActivoTrueOrderByNombreAsc();

	List<Especialidad> findAllByOrderByNombreAsc();

	Optional<Especialidad> findByNombreIgnoreCase(String nombre);
}
