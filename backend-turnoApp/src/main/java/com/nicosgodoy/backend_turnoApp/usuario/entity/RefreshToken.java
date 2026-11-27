package com.nicosgodoy.backend_turnoApp.usuario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "token_hash", nullable = false, unique = true)
	private String tokenHash;

	@Column(name = "expira_en", nullable = false)
	private Instant expiraEn;

	@Column(nullable = false)
	private boolean revocado;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public RefreshToken(Usuario usuario, String tokenHash, Instant expiraEn) {
		this.usuario = usuario;
		this.tokenHash = tokenHash;
		this.expiraEn = expiraEn;
	}

	@PrePersist
	void prePersist() {
		if (this.creadoEn == null) {
			this.creadoEn = Instant.now();
		}
	}
}
