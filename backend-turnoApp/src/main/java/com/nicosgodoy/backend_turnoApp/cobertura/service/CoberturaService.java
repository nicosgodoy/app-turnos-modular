package com.nicosgodoy.backend_turnoApp.cobertura.service;

import com.nicosgodoy.backend_turnoApp.cobertura.dto.ObraSocialResponse;
import com.nicosgodoy.backend_turnoApp.cobertura.dto.PlanRequest;
import com.nicosgodoy.backend_turnoApp.cobertura.dto.PlanResponse;
import com.nicosgodoy.backend_turnoApp.cobertura.dto.ObraSocialRequest;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import com.nicosgodoy.backend_turnoApp.cobertura.repository.ObraSocialRepository;
import com.nicosgodoy.backend_turnoApp.cobertura.repository.PlanRepository;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoberturaService {

	private final ObraSocialRepository obraSocialRepository;
	private final PlanRepository planRepository;

	@Transactional(readOnly = true)
	public List<ObraSocialResponse> listarActivas() {
		return obraSocialRepository.findAllByActivoTrueOrderByNombreAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ObraSocialResponse> listarTodas() {
		return obraSocialRepository.findAllByOrderByNombreAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional
	public ObraSocialResponse crearObraSocial(ObraSocialRequest request) {
		ObraSocial obraSocial = new ObraSocial(request.nombre().trim(), request.codigo().trim().toUpperCase());
		return aResponse(obraSocialRepository.save(obraSocial));
	}

	@Transactional
	public ObraSocialResponse actualizarObraSocial(UUID id, ObraSocialRequest request, Boolean activo) {
		ObraSocial obraSocial = obtenerObraSocial(id);
		obraSocial.setNombre(request.nombre().trim());
		obraSocial.setCodigo(request.codigo().trim().toUpperCase());
		if (activo != null) {
			obraSocial.setActivo(activo);
		}
		return aResponse(obraSocial);
	}

	@Transactional
	public ObraSocialResponse cambiarEstadoObraSocial(UUID id, boolean activo) {
		ObraSocial obraSocial = obtenerObraSocial(id);
		obraSocial.setActivo(activo);
		return aResponse(obraSocial);
	}

	@Transactional
	public PlanResponse crearPlan(UUID obraSocialId, PlanRequest request) {
		ObraSocial obraSocial = obtenerObraSocial(obraSocialId);
		Plan plan = new Plan(obraSocial, request.nombre().trim(), request.codigo().trim().toUpperCase());
		return aPlanResponse(planRepository.save(plan));
	}

	@Transactional
	public PlanResponse actualizarPlan(UUID id, PlanRequest request, Boolean activo) {
		Plan plan = planRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el plan indicado."));
		plan.setNombre(request.nombre().trim());
		plan.setCodigo(request.codigo().trim().toUpperCase());
		if (activo != null) {
			plan.setActivo(activo);
		}
		return aPlanResponse(plan);
	}

	@Transactional(readOnly = true)
	public ObraSocial obtenerObraSocial(UUID id) {
		return obraSocialRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe la obra social indicada."));
	}

	@Transactional(readOnly = true)
	public UUID idObraSocialPorCodigo(String codigo) {
		return obraSocialRepository.findByCodigoIgnoreCase(codigo)
				.orElseThrow(() -> ApiException.noEncontrado("No existe la obra social " + codigo + "."))
				.getId();
	}

	@Transactional(readOnly = true)
	public UUID idPlanPorCodigo(UUID obraSocialId, String codigo) {
		return planRepository.findByObraSocialIdAndCodigoIgnoreCase(obraSocialId, codigo)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el plan " + codigo + "."))
				.getId();
	}

	@Transactional(readOnly = true)
	public Plan validarPlan(UUID obraSocialId, UUID planId) {
		if (obraSocialId == null || planId == null) {
			return null;
		}
		return planRepository.findByIdAndObraSocialId(planId, obraSocialId)
				.orElseThrow(() -> ApiException.solicitudInvalida("El plan no pertenece a la obra social indicada."));
	}

	private ObraSocialResponse aResponse(ObraSocial obraSocial) {
		List<PlanResponse> planes = planRepository
				.findByObraSocialIdOrderByNombreAsc(obraSocial.getId()).stream()
				.map(this::aPlanResponse)
				.toList();
		return new ObraSocialResponse(obraSocial.getId(), obraSocial.getNombre(), obraSocial.getCodigo(),
				obraSocial.isActivo(), planes);
	}

	private PlanResponse aPlanResponse(Plan plan) {
		return new PlanResponse(plan.getId(), plan.getNombre(), plan.getCodigo(), plan.isActivo());
	}
}
