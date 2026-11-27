package com.nicosgodoy.backend_turnoApp.agenda.service;

import com.nicosgodoy.backend_turnoApp.agenda.dto.ConsultorioRequest;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ConsultorioResponse;
import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadRequest;
import com.nicosgodoy.backend_turnoApp.agenda.dto.DisponibilidadResponse;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ExcepcionRequest;
import com.nicosgodoy.backend_turnoApp.agenda.dto.ExcepcionResponse;
import com.nicosgodoy.backend_turnoApp.agenda.entity.Consultorio;
import com.nicosgodoy.backend_turnoApp.agenda.entity.Disponibilidad;
import com.nicosgodoy.backend_turnoApp.agenda.entity.ExcepcionAgenda;
import com.nicosgodoy.backend_turnoApp.agenda.repository.ConsultorioRepository;
import com.nicosgodoy.backend_turnoApp.agenda.repository.DisponibilidadRepository;
import com.nicosgodoy.backend_turnoApp.agenda.repository.ExcepcionAgendaRepository;
import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgendaService {

	private final ConsultorioRepository consultorioRepository;
	private final DisponibilidadRepository disponibilidadRepository;
	private final ExcepcionAgendaRepository excepcionAgendaRepository;
	private final MedicoService medicoService;

	@Transactional(readOnly = true)
	public List<ConsultorioResponse> listarConsultorios() {
		return consultorioRepository.findAllByOrderByNombreAsc().stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public UUID idConsultorioPorNombre(String nombre) {
		return consultorioRepository.findByNombreIgnoreCase(nombre)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el consultorio " + nombre + "."))
				.getId();
	}

	@Transactional
	public ConsultorioResponse crearConsultorio(ConsultorioRequest request) {
		if (consultorioRepository.findByNombreIgnoreCase(request.nombre().trim()).isPresent()) {
			throw ApiException.conflicto("Ya existe un consultorio con ese nombre.");
		}
		Consultorio consultorio = new Consultorio();
		consultorio.setNombre(request.nombre().trim());
		consultorio.setUbicacion(request.ubicacion());
		return aResponse(consultorioRepository.save(consultorio));
	}

	@Transactional
	public ConsultorioResponse actualizarConsultorio(UUID id, ConsultorioRequest request, Boolean activo) {
		Consultorio consultorio = consultorioRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el consultorio indicado."));
		consultorio.setNombre(request.nombre().trim());
		consultorio.setUbicacion(request.ubicacion());
		if (activo != null) {
			consultorio.setActivo(activo);
		}
		return aResponse(consultorio);
	}

	@Transactional(readOnly = true)
	public List<DisponibilidadResponse> listarDisponibilidad(UUID medicoId) {
		return disponibilidadRepository.findByMedicoIdOrderByDiaSemanaAscHoraDesdeAsc(medicoId).stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional
	public DisponibilidadResponse crearDisponibilidad(UUID medicoId, DisponibilidadRequest request) {
		Medico medico = medicoService.obtener(medicoId);
		Consultorio consultorio = obtenerConsultorio(request.consultorioId());
		validarDisponibilidad(request, medicoId, null);

		Disponibilidad disponibilidad = new Disponibilidad();
		disponibilidad.setMedico(medico);
		disponibilidad.setConsultorio(consultorio);
		aplicar(disponibilidad, request);
		return aResponse(disponibilidadRepository.save(disponibilidad));
	}

	@Transactional
	public DisponibilidadResponse actualizarDisponibilidad(UUID id, DisponibilidadRequest request, Boolean activo) {
		Disponibilidad disponibilidad = disponibilidadRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el bloque de disponibilidad indicado."));
		Consultorio consultorio = obtenerConsultorio(request.consultorioId());
		validarDisponibilidad(request, disponibilidad.getMedico().getId(), id);
		disponibilidad.setConsultorio(consultorio);
		aplicar(disponibilidad, request);
		if (activo != null) {
			disponibilidad.setActivo(activo);
		}
		return aResponse(disponibilidad);
	}

	@Transactional
	public DisponibilidadResponse cambiarEstadoDisponibilidad(UUID id, boolean activo) {
		Disponibilidad disponibilidad = disponibilidadRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el bloque de disponibilidad indicado."));
		disponibilidad.setActivo(activo);
		return aResponse(disponibilidad);
	}

	@Transactional(readOnly = true)
	public List<ExcepcionResponse> listarExcepciones(UUID medicoId) {
		return excepcionAgendaRepository.findByMedicoIdOrderByFechaDesdeAsc(medicoId).stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional
	public ExcepcionResponse crearExcepcion(UUID medicoId, ExcepcionRequest request) {
		Medico medico = medicoService.obtener(medicoId);
		validarExcepcion(request);
		ExcepcionAgenda excepcion = new ExcepcionAgenda();
		excepcion.setMedico(medico);
		aplicar(excepcion, request);
		return aResponse(excepcionAgendaRepository.save(excepcion));
	}

	@Transactional
	public void eliminarExcepcion(UUID id) {
		ExcepcionAgenda excepcion = excepcionAgendaRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe la excepción indicada."));
		excepcionAgendaRepository.delete(excepcion);
	}

	private void validarDisponibilidad(DisponibilidadRequest request, UUID medicoId, UUID excluirId) {
		if (!request.horaDesde().isBefore(request.horaHasta())) {
			throw ApiException.solicitudInvalida("La hora de inicio debe ser anterior a la hora de fin.");
		}
		if (request.vigenteHasta() != null && request.vigenteHasta().isBefore(request.vigenteDesde())) {
			throw ApiException.solicitudInvalida("La vigencia final no puede ser anterior a la inicial.");
		}
		boolean solapa = disponibilidadRepository
				.findByMedicoIdOrderByDiaSemanaAscHoraDesdeAsc(medicoId).stream()
				.filter(bloque -> bloque.isActivo())
				.filter(bloque -> excluirId == null || !bloque.getId().equals(excluirId))
				.filter(bloque -> bloque.getDiaSemana() == request.diaSemana())
				.anyMatch(bloque -> bloque.getHoraDesde().isBefore(request.horaHasta())
						&& bloque.getHoraHasta().isAfter(request.horaDesde()));
		if (solapa) {
			throw ApiException.conflicto("El bloque se superpone con otra disponibilidad del médico.");
		}
	}

	private void validarExcepcion(ExcepcionRequest request) {
		if (request.fechaHasta().isBefore(request.fechaDesde())) {
			throw ApiException.solicitudInvalida("La fecha final no puede ser anterior a la inicial.");
		}
		boolean todoElDia = request.todoElDia() == null || request.todoElDia();
		if (!todoElDia) {
			if (request.horaDesde() == null || request.horaHasta() == null
					|| !request.horaDesde().isBefore(request.horaHasta())) {
				throw ApiException.solicitudInvalida(
						"Para una excepción parcial indicá un rango de horas válido.");
			}
		}
	}

	private void aplicar(Disponibilidad disponibilidad, DisponibilidadRequest request) {
		disponibilidad.setDiaSemana(request.diaSemana());
		disponibilidad.setHoraDesde(request.horaDesde());
		disponibilidad.setHoraHasta(request.horaHasta());
		disponibilidad.setVigenteDesde(request.vigenteDesde());
		disponibilidad.setVigenteHasta(request.vigenteHasta());
	}

	private void aplicar(ExcepcionAgenda excepcion, ExcepcionRequest request) {
		boolean todoElDia = request.todoElDia() == null || request.todoElDia();
		excepcion.setFechaDesde(request.fechaDesde());
		excepcion.setFechaHasta(request.fechaHasta());
		excepcion.setTodoElDia(todoElDia);
		excepcion.setHoraDesde(todoElDia ? null : request.horaDesde());
		excepcion.setHoraHasta(todoElDia ? null : request.horaHasta());
		excepcion.setMotivo(request.motivo());
	}

	public Consultorio obtenerConsultorio(UUID id) {
		Consultorio consultorio = consultorioRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el consultorio indicado."));
		if (!consultorio.isActivo()) {
			throw ApiException.solicitudInvalida("El consultorio está inactivo.");
		}
		return consultorio;
	}

	public ConsultorioResponse aResponse(Consultorio consultorio) {
		return new ConsultorioResponse(consultorio.getId(), consultorio.getNombre(),
				consultorio.getUbicacion(), consultorio.isActivo());
	}

	public DisponibilidadResponse aResponse(Disponibilidad disponibilidad) {
		return new DisponibilidadResponse(
				disponibilidad.getId(),
				disponibilidad.getMedico().getId(),
				disponibilidad.getConsultorio().getId(),
				disponibilidad.getConsultorio().getNombre(),
				disponibilidad.getDiaSemana(),
				disponibilidad.getHoraDesde(),
				disponibilidad.getHoraHasta(),
				disponibilidad.getVigenteDesde(),
				disponibilidad.getVigenteHasta(),
				disponibilidad.isActivo());
	}

	public ExcepcionResponse aResponse(ExcepcionAgenda excepcion) {
		return new ExcepcionResponse(
				excepcion.getId(),
				excepcion.getMedico().getId(),
				excepcion.getFechaDesde(),
				excepcion.getFechaHasta(),
				excepcion.isTodoElDia(),
				excepcion.getHoraDesde(),
				excepcion.getHoraHasta(),
				excepcion.getMotivo());
	}
}
