package com.nicosgodoy.backend_turnoApp.shared.security;

import com.nicosgodoy.backend_turnoApp.shared.config.JwtProperties;
import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey clave;
	private final JwtProperties propiedades;
	private final Clock clock;

	public JwtService(JwtProperties propiedades, Clock clock) {
		this.propiedades = propiedades;
		this.clock = clock;
		this.clave = Keys.hmacShaKeyFor(propiedades.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generarAccessToken(UsuarioPrincipal principal) {
		Date ahora = Date.from(clock.instant());
		Date expiracion = Date.from(clock.instant().plus(Duration.ofMinutes(propiedades.accessMinutes())));
		return Jwts.builder()
				.subject(principal.id().toString())
				.claim("tipo", "access")
				.claim("rol", principal.rol().name())
				.claim("email", principal.email())
				.claim("nombre", principal.nombre())
				.issuedAt(ahora)
				.expiration(expiracion)
				.signWith(clave)
				.compact();
	}

	public String generarRefreshToken(UUID usuarioId) {
		Date ahora = Date.from(clock.instant());
		Date expiracion = Date.from(clock.instant().plus(Duration.ofDays(propiedades.refreshDays())));
		return Jwts.builder()
				.subject(usuarioId.toString())
				.id(UUID.randomUUID().toString())
				.claim("tipo", "refresh")
				.issuedAt(ahora)
				.expiration(expiracion)
				.signWith(clave)
				.compact();
	}

	public Claims parsear(String token) {
		return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
	}

	public UsuarioPrincipal principalDesdeAccessToken(Claims claims) {
		return new UsuarioPrincipal(
				UUID.fromString(claims.getSubject()),
				claims.get("email", String.class),
				claims.get("nombre", String.class),
				Rol.valueOf(claims.get("rol", String.class)));
	}

	public int minutosAccessToken() {
		return propiedades.accessMinutes();
	}

	public int diasRefreshToken() {
		return propiedades.refreshDays();
	}
}
