package com.nicosgodoy.backend_turnoApp.notificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "notificacion")
public class Notificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "turno_id", nullable = false)
	private UUID turnoId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoNotificacion tipo;

	@Column(nullable = false)
	private String destinatario;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoNotificacion estado;

	@Column(name = "enviado_en")
	private Instant enviadoEn;

	private String error;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@PrePersist
	void prePersist() {
		if (this.creadoEn == null) {
			this.creadoEn = Instant.now();
		}
	}
}
