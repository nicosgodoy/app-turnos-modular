package com.nicosgodoy.backend_turnoApp.medico.entity;

import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "especialidad")
public class Especialidad extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String nombre;

	@Column(name = "duracion_default_min", nullable = false)
	private int duracionDefaultMin = 30;

	@Column(nullable = false)
	private boolean activo = true;

	public Especialidad(String nombre, int duracionDefaultMin) {
		this.nombre = nombre;
		this.duracionDefaultMin = duracionDefaultMin;
	}
}
