package com.nicosgodoy.backend_turnoApp.turno.repository;

import com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno;
import com.nicosgodoy.backend_turnoApp.turno.entity.Turno;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoRepository extends JpaRepository<Turno, UUID>, JpaSpecificationExecutor<Turno> {

	List<Turno> findByPacienteIdOrderByFechaHoraDesc(UUID pacienteId);

	Optional<Turno> findByIdAndPacienteId(UUID id, UUID pacienteId);

	List<Turno> findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(UUID medicoId,
			OffsetDateTime desde, OffsetDateTime hasta);

	List<Turno> findByEstadoInAndFechaHoraBetweenOrderByFechaHoraAsc(Collection<EstadoTurno> estados,
			OffsetDateTime desde, OffsetDateTime hasta);

	@Query("""
			select t.fechaHora from Turno t
			where t.medico.id = :medicoId
			  and t.fechaHora >= :desde
			  and t.fechaHora < :hasta
			  and t.estado not in (com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno.CANCELADO_PACIENTE,
			                       com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno.CANCELADO_CLINICA)
			""")
	List<OffsetDateTime> fechasOcupadas(@Param("medicoId") UUID medicoId,
			@Param("desde") OffsetDateTime desde,
			@Param("hasta") OffsetDateTime hasta);
}
