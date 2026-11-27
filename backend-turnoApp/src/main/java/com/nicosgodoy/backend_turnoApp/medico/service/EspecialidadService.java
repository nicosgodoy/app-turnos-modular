package com.nicosgodoy.backend_turnoApp.medico.service;

import com.nicosgodoy.backend_turnoApp.medico.dto.EspecialidadRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.EspecialidadResponse;
import com.nicosgodoy.backend_turnoApp.medico.entity.Especialidad;
import com.nicosgodoy.backend_turnoApp.medico.repository.EspecialidadRepository;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EspecialidadService {

	private final EspecialidadRepository especialidadRepository;

	@Transactional(readOnly = true)
	public List<EspecialidadResponse> listarActivas() {
		return especialidadRepository.findAllByActivoTrueOrderByNombreAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<EspecialidadResponse> listarTodas() {
		return especialidadRepository.findAllByOrderByNombreAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public Especialidad obtener(UUID id) {
		return especialidadRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe la especialidad indicada."));
	}

	@Transactional(readOnly = true)
	public UUID idPorNombre(String nombre) {
		return especialidadRepository.findByNombreIgnoreCase(nombre)
				.orElseThrow(() -> ApiException.noEncontrado("No existe la especialidad " + nombre + "."))
				.getId();
	}

	@Transactional
	public EspecialidadResponse crear(EspecialidadRequest request) {
		Especialidad especialidad = new Especialidad(request.nombre().trim(), request.duracionDefaultMin());
		return aResponse(especialidadRepository.save(especialidad));
	}

	@Transactional
	public EspecialidadResponse actualizar(UUID id, EspecialidadRequest request, Boolean activo) {
		Especialidad especialidad = obtener(id);
		especialidad.setNombre(request.nombre().trim());
		especialidad.setDuracionDefaultMin(request.duracionDefaultMin());
		if (activo != null) {
			especialidad.setActivo(activo);
		}
		return aResponse(especialidad);
	}

	public EspecialidadResponse aResponse(Especialidad especialidad) {
		return new EspecialidadResponse(especialidad.getId(), especialidad.getNombre(),
				especialidad.getDuracionDefaultMin(), especialidad.isActivo());
	}
}
