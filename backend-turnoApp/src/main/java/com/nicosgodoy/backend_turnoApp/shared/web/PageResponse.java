package com.nicosgodoy.backend_turnoApp.shared.web;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> contenido, int pagina, int tamanio, long total, int totalPaginas) {

	public static <E, T> PageResponse<T> de(Page<E> pagina, Function<E, T> mapper) {
		return new PageResponse<>(
				pagina.getContent().stream().map(mapper).toList(),
				pagina.getNumber(),
				pagina.getSize(),
				pagina.getTotalElements(),
				pagina.getTotalPages());
	}
}
