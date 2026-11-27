package com.nicosgodoy.backend_turnoApp.agenda.entity;

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
@Table(name = "consultorio")
public class Consultorio extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String nombre;

	private String ubicacion;

	@Column(nullable = false)
	private boolean activo = true;
}
