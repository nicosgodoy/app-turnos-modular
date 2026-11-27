package com.nicosgodoy.backend_turnoApp.paciente.service;

import com.nicosgodoy.backend_turnoApp.cobertura.service.CoberturaService;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import com.nicosgodoy.backend_turnoApp.paciente.dto.ActualizarPacienteRequest;
import com.nicosgodoy.backend_turnoApp.paciente.dto.PacienteResponse;
import com.nicosgodoy.backend_turnoApp.paciente.entity.Paciente;
import com.nicosgodoy.backend_turnoApp.paciente.repository.PacienteRepository;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PacienteService {

	private final PacienteRepository pacienteRepository;
	private final CoberturaService coberturaService;

	@Transactional(readOnly = true)
	public Optional<Paciente> buscarPorDni(String dni) {
		return pacienteRepository.findByDni(normalizarDni(dni));
	}

	@Transactional(readOnly = true)
	public Paciente obtener(UUID id) {
		return pacienteRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el paciente indicado."));
	}

	@Transactional(readOnly = true)
	public Paciente obtenerPorUsuario(UUID usuarioId) {
		return pacienteRepository.findByUsuarioId(usuarioId)
				.orElseThrow(() -> ApiException.noEncontrado("No existe un paciente asociado a tu usuario."));
	}

	@Transactional
	public Paciente crear(Usuario usuario, DatosPaciente datos, boolean consentimiento) {
		Paciente paciente = new Paciente();
		paciente.setUsuario(usuario);
		completar(paciente, datos);
		paciente.setConsentimientoDatos(consentimiento);
		return pacienteRepository.save(paciente);
	}

	@Transactional
	public Paciente vincularUsuario(Paciente paciente, Usuario usuario) {
		paciente.setUsuario(usuario);
		return paciente;
	}

	@Transactional
	public Paciente actualizar(UUID id, ActualizarPacienteRequest request) {
		Paciente paciente = obtener(id);
		paciente.setNombre(request.nombre().trim());
		paciente.setApellido(request.apellido().trim());
		paciente.setFechaNacimiento(request.fechaNacimiento());
		paciente.setTelefono(request.telefono().trim());
		paciente.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
		aplicarCobertura(paciente, request.obraSocialId(), request.planId());
		paciente.setNumeroAfiliado(limpiar(request.numeroAfiliado()));
		if (request.consentimientoDatos() != null) {
			paciente.setConsentimientoDatos(request.consentimientoDatos());
		}
		return paciente;
	}

	@Transactional
	public Paciente actualizarMisDatos(UUID usuarioId, String telefono, UUID obraSocialId, UUID planId,
			String numeroAfiliado) {
		Paciente paciente = obtenerPorUsuario(usuarioId);
		paciente.setTelefono(telefono.trim());
		aplicarCobertura(paciente, obraSocialId, planId);
		paciente.setNumeroAfiliado(limpiar(numeroAfiliado));
		return paciente;
	}

	@Transactional(readOnly = true)
	public Page<Paciente> buscar(String q, Pageable pageable) {
		String filtro = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
		Specification<Paciente> spec = (root, query, cb) -> {
			List<Predicate> condiciones = new ArrayList<>();
			if (filtro != null) {
				String patron = "%" + filtro + "%";
				condiciones.add(cb.or(
						cb.like(cb.lower(root.get("apellido")), patron),
						cb.like(cb.lower(root.get("nombre")), patron),
						cb.like(root.get("dni"), filtro + "%")));
			}
			return cb.and(condiciones.toArray(Predicate[]::new));
		};
		return pacienteRepository.findAll(spec, pageable);
	}

	public PacienteResponse aResponse(Paciente paciente) {
		return new PacienteResponse(
				paciente.getId(),
				paciente.getNombre(),
				paciente.getApellido(),
				paciente.getDni(),
				paciente.getFechaNacimiento(),
				paciente.getTelefono(),
				paciente.getEmail(),
				paciente.getObraSocial() == null ? null
						: new PacienteResponse.CoberturaResumen(paciente.getObraSocial().getId(),
								paciente.getObraSocial().getNombre()),
				paciente.getPlan() == null ? null
						: new PacienteResponse.CoberturaResumen(paciente.getPlan().getId(),
								paciente.getPlan().getNombre()),
				paciente.getNumeroAfiliado(),
				paciente.isConsentimientoDatos());
	}

	private void completar(Paciente paciente, DatosPaciente datos) {
		paciente.setNombre(datos.nombre().trim());
		paciente.setApellido(datos.apellido().trim());
		paciente.setDni(normalizarDni(datos.dni()));
		paciente.setFechaNacimiento(datos.fechaNacimiento());
		paciente.setTelefono(datos.telefono().trim());
		paciente.setEmail(datos.email().trim().toLowerCase(Locale.ROOT));
		paciente.setNumeroAfiliado(limpiar(datos.numeroAfiliado()));
		aplicarCobertura(paciente, datos.obraSocialId(), datos.planId());
	}

	private void aplicarCobertura(Paciente paciente, UUID obraSocialId, UUID planId) {
		if (obraSocialId == null) {
			paciente.setObraSocial(null);
			paciente.setPlan(null);
			return;
		}
		ObraSocial obraSocial = coberturaService.obtenerObraSocial(obraSocialId);
		Plan plan = coberturaService.validarPlan(obraSocialId, planId);
		paciente.setObraSocial(obraSocial);
		paciente.setPlan(plan);
	}

	public static String normalizarDni(String dni) {
		if (dni == null) {
			throw ApiException.solicitudInvalida("El DNI es obligatorio.");
		}
		String normalizado = dni.replaceAll("[^0-9]", "");
		if (normalizado.length() < 7 || normalizado.length() > 9) {
			throw ApiException.solicitudInvalida("El DNI debe tener entre 7 y 9 dígitos.");
		}
		return normalizado;
	}

	private String limpiar(String valor) {
		return (valor == null || valor.isBlank()) ? null : valor.trim();
	}
}
