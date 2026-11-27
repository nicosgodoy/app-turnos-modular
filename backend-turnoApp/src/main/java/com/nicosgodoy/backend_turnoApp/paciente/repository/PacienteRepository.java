package com.nicosgodoy.backend_turnoApp.paciente.repository;

import com.nicosgodoy.backend_turnoApp.paciente.entity.Paciente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PacienteRepository extends JpaRepository<Paciente, UUID>, JpaSpecificationExecutor<Paciente> {

	Optional<Paciente> findByDni(String dni);

	Optional<Paciente> findByUsuarioId(UUID usuarioId);
}
