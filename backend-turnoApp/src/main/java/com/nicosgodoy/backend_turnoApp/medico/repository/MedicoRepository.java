package com.nicosgodoy.backend_turnoApp.medico.repository;

import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicoRepository extends JpaRepository<Medico, UUID> {

	Optional<Medico> findByUsuarioId(UUID usuarioId);

	Optional<Medico> findByMatriculaIgnoreCase(String matricula);

	boolean existsByMatriculaIgnoreCase(String matricula);

	List<Medico> findAllByOrderByApellidoAscNombreAsc();

	@Query("""
			select distinct m from Medico m
			left join m.especialidades e
			where m.activo = true
			  and (:especialidadId is null or e.id = :especialidadId)
			  and (:obraSocialId is null or exists (
			      select 1 from MedicoCobertura c
			      where c.medico = m
			        and c.obraSocial.id = :obraSocialId
			        and (:planId is null or c.plan is null or c.plan.id = :planId)
			  ))
			order by m.apellido asc, m.nombre asc
			""")
	List<Medico> buscar(@Param("especialidadId") UUID especialidadId,
			@Param("obraSocialId") UUID obraSocialId,
			@Param("planId") UUID planId);
}
