package com.nicosgodoy.backend_turnoApp.agenda.repository;

import com.nicosgodoy.backend_turnoApp.agenda.entity.Disponibilidad;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, UUID> {

	List<Disponibilidad> findByMedicoIdOrderByDiaSemanaAscHoraDesdeAsc(UUID medicoId);

	@Query("""
			select d from Disponibilidad d
			where d.medico.id = :medicoId
			  and d.activo = true
			  and d.diaSemana = :diaSemana
			  and d.vigenteDesde <= :fecha
			  and (d.vigenteHasta is null or d.vigenteHasta >= :fecha)
			order by d.horaDesde asc
			""")
	List<Disponibilidad> buscarVigentes(@Param("medicoId") UUID medicoId,
			@Param("diaSemana") int diaSemana,
			@Param("fecha") LocalDate fecha);
}
