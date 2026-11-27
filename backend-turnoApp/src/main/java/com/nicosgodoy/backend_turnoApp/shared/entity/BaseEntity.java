package com.nicosgodoy.backend_turnoApp.shared.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	@Column(name = "actualizado_en", nullable = false)
	private Instant actualizadoEn;

	@PrePersist
	void prePersist() {
		Instant ahora = Instant.now();
		this.creadoEn = ahora;
		this.actualizadoEn = ahora;
	}

	@PreUpdate
	void preUpdate() {
		this.actualizadoEn = Instant.now();
	}
}
