package com.nicosgodoy.backend_turnoApp.agenda.repository;

import com.nicosgodoy.backend_turnoApp.agenda.entity.ExcepcionAgenda;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExcepcionAgendaRepository extends JpaRepository<ExcepcionAgenda, UUID> {

	List<ExcepcionAgenda> findByMedicoIdOrderByFechaDesdeAsc(UUID medicoId);

	@Query("""
			select e from ExcepcionAgenda e
			where e.medico.id = :medicoId
			  and e.fechaDesde <= :fecha
			  and e.fechaHasta >= :fecha
			order by e.fechaDesde asc
			""")
	List<ExcepcionAgenda> buscarDelDia(@Param("medicoId") UUID medicoId, @Param("fecha") LocalDate fecha);
}
