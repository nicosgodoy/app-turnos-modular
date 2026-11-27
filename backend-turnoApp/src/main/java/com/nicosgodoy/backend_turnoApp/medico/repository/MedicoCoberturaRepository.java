package com.nicosgodoy.backend_turnoApp.medico.repository;

import com.nicosgodoy.backend_turnoApp.medico.entity.MedicoCobertura;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicoCoberturaRepository extends JpaRepository<MedicoCobertura, UUID> {

	List<MedicoCobertura> findByMedicoIdOrderByObraSocialNombreAsc(UUID medicoId);

	@Modifying(flushAutomatically = true)
	@Query("delete from MedicoCobertura c where c.medico.id = :medicoId")
	void deleteByMedicoId(@Param("medicoId") UUID medicoId);

	@Query("""
			select count(c) > 0 from MedicoCobertura c
			where c.medico.id = :medicoId
			  and c.obraSocial.id = :obraSocialId
			  and c.plan is null
			""")
	boolean aceptaObraSocialSinPlan(@Param("medicoId") UUID medicoId,
			@Param("obraSocialId") UUID obraSocialId);

	@Query("""
			select count(c) > 0 from MedicoCobertura c
			where c.medico.id = :medicoId
			  and c.obraSocial.id = :obraSocialId
			  and (c.plan is null or c.plan.id = :planId)
			""")
	boolean aceptaPlan(@Param("medicoId") UUID medicoId,
			@Param("obraSocialId") UUID obraSocialId,
			@Param("planId") UUID planId);
}
