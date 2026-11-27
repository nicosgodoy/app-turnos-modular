package com.nicosgodoy.backend_turnoApp.medico.service;

import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import com.nicosgodoy.backend_turnoApp.cobertura.service.CoberturaService;
import com.nicosgodoy.backend_turnoApp.medico.dto.ActualizarMedicoRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.CoberturaRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.CoberturaResponse;
import com.nicosgodoy.backend_turnoApp.medico.dto.CrearMedicoRequest;
import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResumenResponse;
import com.nicosgodoy.backend_turnoApp.medico.dto.MedicoResponse;
import com.nicosgodoy.backend_turnoApp.medico.entity.Especialidad;
import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import com.nicosgodoy.backend_turnoApp.medico.entity.MedicoCobertura;
import com.nicosgodoy.backend_turnoApp.medico.repository.EspecialidadRepository;
import com.nicosgodoy.backend_turnoApp.medico.repository.MedicoCoberturaRepository;
import com.nicosgodoy.backend_turnoApp.medico.repository.MedicoRepository;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import com.nicosgodoy.backend_turnoApp.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicoService {

	private final MedicoRepository medicoRepository;
	private final MedicoCoberturaRepository medicoCoberturaRepository;
	private final EspecialidadRepository especialidadRepository;
	private final UsuarioRepository usuarioRepository;
	private final CoberturaService coberturaService;
	private final EspecialidadService especialidadService;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<MedicoResumenResponse> buscar(UUID especialidadId, UUID obraSocialId, UUID planId) {
		return medicoRepository.buscar(especialidadId, obraSocialId, planId).stream()
				.map(this::aResumen)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MedicoResponse> listarTodos() {
		return medicoRepository.findAllByOrderByApellidoAscNombreAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public Medico obtener(UUID id) {
		return medicoRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el médico indicado."));
	}

	@Transactional(readOnly = true)
	public Medico obtenerActivo(UUID id) {
		Medico medico = obtener(id);
		if (!medico.isActivo()) {
			throw ApiException.solicitudInvalida("El médico no está disponible para reservar turnos.");
		}
		return medico;
	}

	@Transactional(readOnly = true)
	public Medico porUsuario(UUID usuarioId) {
		return medicoRepository.findByUsuarioId(usuarioId)
				.orElseThrow(() -> ApiException.noEncontrado("No existe un médico asociado a tu usuario."));
	}

	@Transactional(readOnly = true)
	public MedicoResponse obtenerResponse(UUID id) {
		return aResponse(obtener(id));
	}

	@Transactional(readOnly = true)
	public MedicoResponse perfil(UUID usuarioId) {
		return aResponse(porUsuario(usuarioId));
	}

	@Transactional
	public MedicoResponse crear(CrearMedicoRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw ApiException.conflicto("Ya existe un usuario con ese email.");
		}
		if (medicoRepository.existsByMatriculaIgnoreCase(request.matricula().trim())) {
			throw ApiException.conflicto("Ya existe un médico con esa matrícula.");
		}

		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		usuario.setNombre(request.nombre().trim());
		usuario.setApellido(request.apellido().trim());
		usuario.setRol(Rol.MEDICO);
		usuario.setActivo(true);
		usuario.setPasswordHash(passwordEncoder.encode(request.password()));
		usuarioRepository.save(usuario);

		Medico medico = new Medico();
		medico.setUsuario(usuario);
		medico.setNombre(request.nombre().trim());
		medico.setApellido(request.apellido().trim());
		medico.setMatricula(request.matricula().trim());
		medico.setDuracionTurnoMin(request.duracionTurnoMin());
		medico.setPrecioParticular(valor(request.precioParticular()));
		medico.setBiografia(request.biografia());
		medico.setActivo(true);
		medico.setEspecialidades(resolverEspecialidades(request.especialidadIds()));
		medicoRepository.save(medico);

		guardarCoberturas(medico, request.coberturas());
		return aResponse(medico);
	}

	@Transactional
	public MedicoResponse actualizar(UUID id, ActualizarMedicoRequest request) {
		Medico medico = obtener(id);
		Usuario usuario = medico.getUsuario();
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw ApiException.conflicto("Ya existe un usuario con ese email.");
		}
		if (!medico.getMatricula().equalsIgnoreCase(request.matricula().trim())
				&& medicoRepository.existsByMatriculaIgnoreCase(request.matricula().trim())) {
			throw ApiException.conflicto("Ya existe un médico con esa matrícula.");
		}

		usuario.setEmail(email);
		usuario.setNombre(request.nombre().trim());
		usuario.setApellido(request.apellido().trim());
		if (request.password() != null && !request.password().isBlank()) {
			usuario.setPasswordHash(passwordEncoder.encode(request.password()));
		}

		medico.setNombre(request.nombre().trim());
		medico.setApellido(request.apellido().trim());
		medico.setMatricula(request.matricula().trim());
		medico.setDuracionTurnoMin(request.duracionTurnoMin());
		medico.setPrecioParticular(valor(request.precioParticular()));
		medico.setBiografia(request.biografia());
		medico.setEspecialidades(resolverEspecialidades(request.especialidadIds()));
		if (request.activo() != null) {
			medico.setActivo(request.activo());
			usuario.setActivo(request.activo());
		}

		medicoCoberturaRepository.deleteByMedicoId(id);
		guardarCoberturas(medico, request.coberturas());
		return aResponse(medico);
	}

	@Transactional
	public MedicoResponse cambiarEstado(UUID id, boolean activo) {
		Medico medico = obtener(id);
		medico.setActivo(activo);
		medico.getUsuario().setActivo(activo);
		return aResponse(medico);
	}

	@Transactional(readOnly = true)
	public boolean existeMatricula(String matricula) {
		return medicoRepository.existsByMatriculaIgnoreCase(matricula);
	}

	@Transactional(readOnly = true)
	public java.util.Optional<UUID> idPorMatricula(String matricula) {
		return medicoRepository.findByMatriculaIgnoreCase(matricula).map(Medico::getId);
	}

	@Transactional(readOnly = true)
	public boolean aceptaCobertura(UUID medicoId, UUID obraSocialId, UUID planId) {
		if (obraSocialId == null) {
			return false;
		}
		if (planId == null) {
			return medicoCoberturaRepository.aceptaObraSocialSinPlan(medicoId, obraSocialId);
		}
		return medicoCoberturaRepository.aceptaPlan(medicoId, obraSocialId, planId);
	}

	@Transactional(readOnly = true)
	public MedicoResumenResponse aResumen(Medico medico) {
		return new MedicoResumenResponse(
				medico.getId(),
				medico.getNombre(),
				medico.getApellido(),
				medico.getMatricula(),
				medico.getBiografia(),
				medico.getDuracionTurnoMin(),
				medico.getPrecioParticular(),
				medico.getEspecialidades().stream().map(especialidadService::aResponse).toList());
	}

	@Transactional(readOnly = true)
	public MedicoResponse aResponse(Medico medico) {
		List<CoberturaResponse> coberturas = medicoCoberturaRepository
				.findByMedicoIdOrderByObraSocialNombreAsc(medico.getId()).stream()
				.map(this::aCobertura)
				.toList();
		return new MedicoResponse(
				medico.getId(),
				medico.getNombre(),
				medico.getApellido(),
				medico.getUsuario().getEmail(),
				medico.getMatricula(),
				medico.getBiografia(),
				medico.getDuracionTurnoMin(),
				medico.getPrecioParticular(),
				medico.isActivo(),
				medico.getEspecialidades().stream().map(especialidadService::aResponse).toList(),
				coberturas);
	}

	private CoberturaResponse aCobertura(MedicoCobertura cobertura) {
		return new CoberturaResponse(
				cobertura.getObraSocial().getId(),
				cobertura.getObraSocial().getNombre(),
				cobertura.getPlan() == null ? null : cobertura.getPlan().getId(),
				cobertura.getPlan() == null ? null : cobertura.getPlan().getNombre());
	}

	private Set<Especialidad> resolverEspecialidades(List<UUID> ids) {
		List<Especialidad> encontradas = especialidadRepository.findAllById(ids);
		if (encontradas.size() != new LinkedHashSet<>(ids).size()) {
			throw ApiException.solicitudInvalida("Alguna de las especialidades indicadas no existe.");
		}
		return new LinkedHashSet<>(encontradas);
	}

	private void guardarCoberturas(Medico medico, List<CoberturaRequest> coberturas) {
		if (coberturas == null || coberturas.isEmpty()) {
			return;
		}
		Map<String, MedicoCobertura> unicas = new LinkedHashMap<>();
		for (CoberturaRequest request : coberturas) {
			if (request.obraSocialId() == null) {
				continue;
			}
			ObraSocial obraSocial = coberturaService.obtenerObraSocial(request.obraSocialId());
			Plan plan = coberturaService.validarPlan(request.obraSocialId(), request.planId());
			String clave = request.obraSocialId() + "|" + (request.planId() == null ? "-" : request.planId());
			unicas.putIfAbsent(clave, new MedicoCobertura(medico, obraSocial, plan));
		}
		medicoCoberturaRepository.saveAll(unicas.values());
	}

	private BigDecimal valor(BigDecimal precio) {
		return precio == null ? BigDecimal.ZERO : precio;
	}
}
