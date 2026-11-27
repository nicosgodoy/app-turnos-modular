package com.nicosgodoy.backend_turnoApp.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String PREFIJO = "Bearer ";

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String cabecera = request.getHeader("Authorization");
		if (cabecera != null && cabecera.startsWith(PREFIJO)) {
			try {
				Claims claims = jwtService.parsear(cabecera.substring(PREFIJO.length()));
				if ("access".equals(claims.get("tipo", String.class))) {
					UsuarioPrincipal principal = jwtService.principalDesdeAccessToken(claims);
					var autenticacion = new UsernamePasswordAuthenticationToken(
							principal,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + principal.rol().name())));
					SecurityContextHolder.getContext().setAuthentication(autenticacion);
				}
			} catch (JwtException | IllegalArgumentException ex) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
