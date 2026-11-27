package com.nicosgodoy.backend_turnoApp.turno.service;

import com.nicosgodoy.backend_turnoApp.agenda.entity.Consultorio;
import com.nicosgodoy.backend_turnoApp.agenda.service.AgendaService;
import com.nicosgodoy.backend_turnoApp.agenda.service.SlotService;
import com.nicosgodoy.backend_turnoApp.agenda.dto.SlotResponse;
import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import com.nicosgodoy.backend_turnoApp.medico.service.MedicoService;
import com.nicosgodoy.backend_turnoApp.notificacion.service.DatosTurnoNotificacion;
import com.nicosgodoy.backend_turnoApp.notificacion.service.NotificacionService;
import com.nicosgodoy.backend_turnoApp.paciente.entity.Paciente;
import com.nicosgodoy.backend_turnoApp.paciente.service.PacienteService;
import com.nicosgodoy.backend_turnoApp.shared.config.TurnoProperties;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.shared.web.PageResponse;
import com.nicosgodoy.backend_turnoApp.turno.dto.ReservarTurnoRequest;
import com.nicosgodoy.backend_turnoApp.turno.dto.TurnoResponse;
import com.nicosgodoy.backend_turnoApp.turno.entity.EstadoTurno;
import com.nicosgodoy.backend_turnoApp.turno.entity.TipoCobertura;
import com.nicosgodoy.backend_turnoApp.turno.entity.Turno;
import com.nicosgodoy.backend_turnoApp.turno.repository.TurnoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

	private final TurnoRepository turnoRepository;
	private final PacienteService pacienteService;
	private final MedicoService medicoService;
	private final AgendaService agendaService;
	private final SlotService slotService;
	private final NotificacionService notificacionService;
	private final TurnoProperties turnoProperties;
	private final Clock clock;
	private final ZoneId zonaHoraria;

	@Transactional
	public TurnoResponse reservar(UUID usuarioId, ReservarTurnoRequest request) {
		Paciente paciente = pacienteService.obtenerPorUsuario(usuarioId);
		Medico medico = medicoService.obtenerActivo(request.medicoId());
		SlotResponse slot = slotService.validarSlot(medico.getId(), request.fechaHora());
		Consultorio consultorio = agendaService.obtenerConsultorio(slot.consultorioId());

		boolean acepta = paciente.getObraSocial() != null
				&& medicoService.aceptaCobertura(medico.getId(), paciente.getObraSocial().getId(),
						paciente.getPlan() == null ? null : paciente.getPlan().getId());

		TipoCobertura tipo;
		if (request.tipoCobertura() == TipoCobertura.PARTICULAR) {
			tipo = TipoCobertura.PARTICULAR;
		} else if (acepta) {
			tipo = TipoCobertura.OBRA_SOCIAL;
		} else if (request.tipoCobertura() == TipoCobertura.OBRA_SOCIAL) {
			throw ApiException.conflicto(
					"El profesional no atiende tu obra social o plan. Podés reservar como particular.");
		} else {
			tipo = TipoCobertura.PARTICULAR;
		}

		Turno turno = new Turno();
		turno.setMedico(medico);
		turno.setPaciente(paciente);
		turno.setConsultorio(consultorio);
		turno.setFechaHora(request.fechaHora());
		turno.setDuracionMin(slot.duracionMin());
		turno.setEstado(EstadoTurno.CONFIRMADO);
		turno.setTipoCobertura(tipo);
		turno.setMotivo(request.motivo());
		turno.setCanal("WEB");
		if (tipo == TipoCobertura.OBRA_SOCIAL) {
			turno.setObraSocial(paciente.getObraSocial());
			turno.setPlan(paciente.getPlan());
			turno.setNumeroAfiliado(paciente.getNumeroAfiliado());
		} else {
			turno.setPrecio(medico.getPrecioParticular());
		}

		try {
			turnoRepository.saveAndFlush(turno);
		} catch (DataIntegrityViolationException ex) {
			throw ApiException.conflicto("Ese horario acaba de ser reservado. Elegí otro.");
		}

		notificacionService.enviarConfirmacion(datosParaNotificacion(turno));
		return aResponse(turno);
	}

	@Transactional(readOnly = true)
	public List<TurnoResponse> misTurnos(UUID usuarioId) {
		Paciente paciente = pacienteService.obtenerPorUsuario(usuarioId);
		return turnoRepository.findByPacienteIdOrderByFechaHoraDesc(paciente.getId()).stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional
	public TurnoResponse cancelar(UUID usuarioId, UUID turnoId, String motivo) {
		Paciente paciente = pacienteService.obtenerPorUsuario(usuarioId);
		Turno turno = turnoRepository.findByIdAndPacienteId(turnoId, paciente.getId())
				.orElseThrow(() -> ApiException.noEncontrado("No existe el turno indicado."));
		validarModificable(turno);
		turno.setEstado(EstadoTurno.CANCELADO_PACIENTE);
		turno.setCanceladoMotivo(motivo);
		notificacionService.enviarCancelacion(datosParaNotificacion(turno));
		return aResponse(turno);
	}

	@Transactional
	public TurnoResponse reprogramar(UUID usuarioId, UUID turnoId, OffsetDateTime nuevaFechaHora) {
		Paciente paciente = pacienteService.obtenerPorUsuario(usuarioId);
		Turno turno = turnoRepository.findByIdAndPacienteId(turnoId, paciente.getId())
				.orElseThrow(() -> ApiException.noEncontrado("No existe el turno indicado."));
		validarModificable(turno);

		SlotResponse slot = slotService.validarSlot(turno.getMedico().getId(), nuevaFechaHora);
		turno.setFechaHora(nuevaFechaHora);
		turno.setConsultorio(agendaService.obtenerConsultorio(slot.consultorioId()));
		turno.setDuracionMin(slot.duracionMin());
		turno.setEstado(EstadoTurno.CONFIRMADO);
		try {
			turnoRepository.saveAndFlush(turno);
		} catch (DataIntegrityViolationException ex) {
			throw ApiException.conflicto("Ese horario acaba de ser reservado. Elegí otro.");
		}

		notificacionService.enviarReprogramacion(datosParaNotificacion(turno));
		return aResponse(turno);
	}

	@Transactional(readOnly = true)
	public PageResponse<TurnoResponse> listarAdmin(LocalDate fecha, UUID medicoId, EstadoTurno estado,
			int page, int size) {
		OffsetDateTime desde = null;
		OffsetDateTime hasta = null;
		if (fecha != null) {
			desde = fecha.atStartOfDay(zonaHoraria).toOffsetDateTime();
			hasta = fecha.plusDays(1).atStartOfDay(zonaHoraria).toOffsetDateTime();
		}
		var resultado = turnoRepository.findAll(construirFiltro(medicoId, estado, desde, hasta),
				PageRequest.of(page, Math.min(size, 100), Sort.by("fechaHora").ascending()));
		return PageResponse.de(resultado, this::aResponse);
	}

	private Specification<Turno> construirFiltro(UUID medicoId, EstadoTurno estado,
			OffsetDateTime desde, OffsetDateTime hasta) {
		return (root, query, cb) -> {
			List<Predicate> condiciones = new ArrayList<>();
			if (medicoId != null) {
				condiciones.add(cb.equal(root.get("medico").get("id"), medicoId));
			}
			if (estado != null) {
				condiciones.add(cb.equal(root.get("estado"), estado));
			}
			if (desde != null) {
				condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde));
			}
			if (hasta != null) {
				condiciones.add(cb.lessThan(root.get("fechaHora"), hasta));
			}
			return cb.and(condiciones.toArray(Predicate[]::new));
		};
	}

	@Transactional(readOnly = true)
	public TurnoResponse obtener(UUID id) {
		return aResponse(turnoRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el turno indicado.")));
	}

	@Transactional(readOnly = true)
	public List<TurnoResponse> turnosDelMedico(UUID usuarioId, LocalDate fecha) {
		Medico medico = medicoService.porUsuario(usuarioId);
		LocalDate dia = fecha == null ? LocalDate.now(clock) : fecha;
		var desde = dia.atStartOfDay(zonaHoraria).toOffsetDateTime();
		var hasta = dia.plusDays(1).atStartOfDay(zonaHoraria).toOffsetDateTime();
		return turnoRepository.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(medico.getId(), desde, hasta)
				.stream()
				.map(this::aResponse)
				.toList();
	}

	@Transactional
	public TurnoResponse cambiarEstado(UUID id, EstadoTurno nuevoEstado, String motivo) {
		if (nuevoEstado == EstadoTurno.CANCELADO_CLINICA) {
			return cancelarPorClinica(id, motivo);
		}
		if (nuevoEstado != EstadoTurno.ATENDIDO && nuevoEstado != EstadoTurno.AUSENTE) {
			throw ApiException.solicitudInvalida("Solo se puede pasar el turno a ATENDIDO o AUSENTE.");
		}
		Turno turno = turnoRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el turno indicado."));
		if (!turno.getEstado().esCancelable()) {
			throw ApiException.conflicto("El turno ya no se puede modificar.");
		}
		turno.setEstado(nuevoEstado);
		return aResponse(turno);
	}

	@Transactional
	public TurnoResponse cancelarPorClinica(UUID id, String motivo) {
		Turno turno = turnoRepository.findById(id)
				.orElseThrow(() -> ApiException.noEncontrado("No existe el turno indicado."));
		if (!turno.getEstado().esCancelable()) {
			throw ApiException.conflicto("El turno ya no se puede cancelar.");
		}
		turno.setEstado(EstadoTurno.CANCELADO_CLINICA);
		turno.setCanceladoMotivo(motivo);
		notificacionService.enviarCancelacion(datosParaNotificacion(turno));
		return aResponse(turno);
	}

	public DatosTurnoNotificacion datosParaNotificacion(Turno turno) {
		return new DatosTurnoNotificacion(
				turno.getId(),
				turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido(),
				turno.getPaciente().getEmail(),
				turno.getMedico().nombreCompleto(),
				turno.getFechaHora(),
				turno.getConsultorio().getNombre(),
				turno.getMotivo());
	}

	public TurnoResponse aResponse(Turno turno) {
		String especialidades = turno.getMedico().getEspecialidades().stream()
				.map(especialidad -> especialidad.getNombre())
				.collect(Collectors.joining(", "));
		return new TurnoResponse(
				turno.getId(),
				turno.getMedico().getId(),
				turno.getMedico().nombreCompleto(),
				especialidades,
				turno.getPaciente().getId(),
				turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido(),
				turno.getPaciente().getDni(),
				turno.getFechaHora(),
				turno.getDuracionMin(),
				turno.getConsultorio().getNombre(),
				turno.getEstado(),
				turno.getTipoCobertura(),
				turno.getObraSocial() == null ? null : turno.getObraSocial().getNombre(),
				turno.getPlan() == null ? null : turno.getPlan().getNombre(),
				turno.getNumeroAfiliado(),
				turno.getPrecio(),
				turno.getMotivo(),
				esCancelable(turno),
				turno.getCreadoEn());
	}

	private boolean esCancelable(Turno turno) {
		if (!turno.getEstado().esCancelable()) {
			return false;
		}
		OffsetDateTime limite = OffsetDateTime.now(clock)
				.plusHours(turnoProperties.cancelacionMinimaHoras());
		return !turno.getFechaHora().isBefore(limite);
	}

	private void validarModificable(Turno turno) {
		if (!turno.getEstado().esCancelable()) {
			throw ApiException.conflicto("El turno ya no se puede modificar.");
		}
		OffsetDateTime limite = OffsetDateTime.now(clock)
				.plusHours(turnoProperties.cancelacionMinimaHoras());
		if (turno.getFechaHora().isBefore(limite)) {
			throw ApiException.prohibido("Solo podés modificar turnos con al menos "
					+ turnoProperties.cancelacionMinimaHoras() + " horas de anticipación.");
		}
	}
}
