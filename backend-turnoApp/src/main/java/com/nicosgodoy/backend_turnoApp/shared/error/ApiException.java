package com.nicosgodoy.backend_turnoApp.shared.error;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private final HttpStatus estado;
	private final String codigo;

	public ApiException(HttpStatus estado, String codigo, String mensaje) {
		super(mensaje);
		this.estado = estado;
		this.codigo = codigo;
	}

	public static ApiException noEncontrado(String mensaje) {
		return new ApiException(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje);
	}

	public static ApiException solicitudInvalida(String mensaje) {
		return new ApiException(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", mensaje);
	}

	public static ApiException conflicto(String mensaje) {
		return new ApiException(HttpStatus.CONFLICT, "CONFLICTO", mensaje);
	}

	public static ApiException noAutorizado(String mensaje) {
		return new ApiException(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", mensaje);
	}

	public static ApiException prohibido(String mensaje) {
		return new ApiException(HttpStatus.FORBIDDEN, "PROHIBIDO", mensaje);
	}

	public static ApiException demasiadasSolicitudes(String mensaje) {
		return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "DEMASIADAS_SOLICITUDES", mensaje);
	}

	public HttpStatus getEstado() {
		return estado;
	}

	public String getCodigo() {
		return codigo;
	}
}
