package com.nicosgodoy.backend_turnoApp.paciente.service;

import com.nicosgodoy.backend_turnoApp.notificacion.service.EmailService;
import com.nicosgodoy.backend_turnoApp.paciente.dto.OtpRequest;
import com.nicosgodoy.backend_turnoApp.paciente.dto.OtpSolicitadoResponse;
import com.nicosgodoy.backend_turnoApp.paciente.dto.OtpVerifyRequest;
import com.nicosgodoy.backend_turnoApp.paciente.entity.Paciente;
import com.nicosgodoy.backend_turnoApp.shared.config.OtpProperties;
import com.nicosgodoy.backend_turnoApp.shared.error.ApiException;
import com.nicosgodoy.backend_turnoApp.usuario.entity.CodigoOtp;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import com.nicosgodoy.backend_turnoApp.usuario.repository.CodigoOtpRepository;
import com.nicosgodoy.backend_turnoApp.usuario.service.TokenService;
import com.nicosgodoy.backend_turnoApp.usuario.service.Tokens;
import com.nicosgodoy.backend_turnoApp.usuario.service.UsuarioService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteAuthService {

	private final PacienteService pacienteService;
	private final UsuarioService usuarioService;
	private final CodigoOtpRepository codigoOtpRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final OtpProperties otpProperties;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	@Transactional
	public OtpSolicitadoResponse solicitarOtp(OtpRequest request) {
		Paciente paciente = resolverPaciente(request);
		Usuario usuario = paciente.getUsuario();

		Instant desde = clock.instant().minus(Duration.ofMinutes(otpProperties.sendWindowMinutes()));
		long envios = codigoOtpRepository.countByUsuario_IdAndCreadoEnAfter(usuario.getId(), desde);
		if (envios >= otpProperties.maxSends()) {
			throw ApiException.demasiadasSolicitudes(
					"Ya enviamos varios códigos. Esperá unos minutos antes de volver a intentar.");
		}

		codigoOtpRepository.invalidarTodos(usuario.getId());
		String codigo = String.format("%06d", random.nextInt(1_000_000));
		Instant expiraEn = clock.instant().plus(Duration.ofMinutes(otpProperties.expirationMinutes()));
		codigoOtpRepository.save(new CodigoOtp(usuario, passwordEncoder.encode(codigo), expiraEn));

		if (otpProperties.logCodigo()) {
			log.info("OTP de desarrollo para {}: {}", paciente.getEmail(), codigo);
		}

		try {
			emailService.enviarCodigoOtp(paciente.getEmail(), paciente.getNombre(), codigo, expiraEn);
		} catch (RuntimeException ex) {
			log.error("No se pudo enviar el OTP a {}", paciente.getEmail(), ex);
			if (!otpProperties.logCodigo()) {
				throw new ApiException(HttpStatus.BAD_GATEWAY, "EMAIL",
						"No pudimos enviar el código. Intentá nuevamente en unos minutos.");
			}
		}

		String enmascarado = enmascarar(paciente.getEmail());
		return new OtpSolicitadoResponse("Te enviamos un código a " + enmascarado + ".",
				otpProperties.expirationMinutes(), enmascarado);
	}

	@Transactional(noRollbackFor = ApiException.class)
	public Tokens verificarOtp(OtpVerifyRequest request) {
		Paciente paciente = pacienteService.buscarPorDni(request.dni())
				.orElseThrow(() -> ApiException.solicitudInvalida("No encontramos una solicitud para ese DNI."));
		Usuario usuario = paciente.getUsuario();
		if (usuario == null) {
			throw ApiException.solicitudInvalida("Solicitá un código nuevo.");
		}

		CodigoOtp otp = codigoOtpRepository
				.findFirstByUsuario_IdAndUsadoFalseOrderByCreadoEnDesc(usuario.getId())
				.orElseThrow(() -> ApiException.solicitudInvalida("Solicitá un código nuevo."));

		if (otp.getExpiraEn().isBefore(clock.instant())) {
			throw ApiException.solicitudInvalida("El código expiró. Solicitá uno nuevo.");
		}
		if (otp.getIntentos() >= otpProperties.maxAttempts()) {
			throw ApiException.demasiadasSolicitudes("Demasiados intentos. Solicitá un código nuevo.");
		}
		if (!passwordEncoder.matches(request.codigo(), otp.getCodigoHash())) {
			otp.setIntentos(otp.getIntentos() + 1);
			codigoOtpRepository.save(otp);
			throw ApiException.solicitudInvalida("El código es incorrecto.");
		}

		otp.setUsado(true);
		codigoOtpRepository.save(otp);
		usuario.setActivo(true);
		return tokenService.generar(usuario);
	}

	private Paciente resolverPaciente(OtpRequest request) {
		String dni = PacienteService.normalizarDni(request.dni());
		String apellido = request.apellido().trim();
		Optional<Paciente> existente = pacienteService.buscarPorDni(dni);

		if (existente.isPresent()) {
			Paciente paciente = existente.get();
			if (!paciente.getApellido().equalsIgnoreCase(apellido)) {
				throw ApiException.solicitudInvalida("Los datos ingresados no coinciden con nuestros registros.");
			}
			if (paciente.getUsuario() == null) {
				Usuario usuario = usuarioService.crearPaciente(paciente.getNombre(), paciente.getApellido(),
						paciente.getEmail());
				return pacienteService.vincularUsuario(paciente, usuario);
			}
			return paciente;
		}

		if (esVacio(request.nombre()) || esVacio(request.email()) || esVacio(request.telefono())) {
			throw ApiException.solicitudInvalida(
					"Para registrarte necesitamos nombre, email y teléfono.");
		}
		if (!Boolean.TRUE.equals(request.consentimiento())) {
			throw ApiException.solicitudInvalida(
					"Necesitamos tu consentimiento para tratar los datos personales.");
		}

		Usuario usuario = usuarioService.crearPaciente(request.nombre(), apellido, request.email());
		DatosPaciente datos = new DatosPaciente(
				request.nombre(),
				apellido,
				dni,
				request.fechaNacimiento(),
				request.telefono(),
				request.email(),
				request.obraSocialId(),
				request.planId(),
				request.numeroAfiliado());
		return pacienteService.crear(usuario, datos, true);
	}

	private boolean esVacio(String valor) {
		return valor == null || valor.isBlank();
	}

	private String enmascarar(String email) {
		int arroba = email.indexOf('@');
		if (arroba <= 1) {
			return "***" + email.substring(Math.max(arroba, 0));
		}
		return email.charAt(0) + "***" + email.substring(arroba);
	}
}
