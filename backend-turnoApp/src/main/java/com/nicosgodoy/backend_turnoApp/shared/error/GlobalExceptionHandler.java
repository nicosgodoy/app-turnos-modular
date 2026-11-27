package com.nicosgodoy.backend_turnoApp.shared.error;

import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ProblemDetail manejarApi(ApiException ex) {
		ProblemDetail problema = problema(ex.getEstado(), ex.getMessage());
		problema.setProperty("codigo", ex.getCodigo());
		return problema;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
		Map<String, String> errores = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud.");
		problema.setProperty("codigo", "VALIDACION");
		problema.setProperty("errores", errores);
		return problema;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail manejarRestricciones(ConstraintViolationException ex) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud.");
		problema.setProperty("codigo", "VALIDACION");
		problema.setProperty("errores", ex.getMessage());
		return problema;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail manejarJsonInvalido(HttpMessageNotReadableException ex) {
		ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es válido.");
		problema.setProperty("codigo", "JSON_INVALIDO");
		return problema;
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail manejarIntegridad(DataIntegrityViolationException ex) {
		log.warn("Conflicto de integridad: {}", ex.getMostSpecificCause().getMessage());
		ProblemDetail problema = problema(HttpStatus.CONFLICT, "La operación entra en conflicto con datos existentes.");
		problema.setProperty("codigo", "CONFLICTO");
		return problema;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail manejarAccesoDenegado(AccessDeniedException ex) {
		ProblemDetail problema = problema(HttpStatus.FORBIDDEN, "No tenés permiso para realizar esta acción.");
		problema.setProperty("codigo", "PROHIBIDO");
		return problema;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail manejarInesperado(Exception ex) {
		log.error("Error inesperado", ex);
		ProblemDetail problema = problema(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.");
		problema.setProperty("codigo", "ERROR_INTERNO");
		return problema;
	}

	private ProblemDetail problema(HttpStatus estado, String mensaje) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, mensaje);
		problema.setTitle(estado.getReasonPhrase());
		return problema;
	}
}
