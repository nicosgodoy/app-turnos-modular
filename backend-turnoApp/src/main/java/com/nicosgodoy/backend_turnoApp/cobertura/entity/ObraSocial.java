package com.nicosgodoy.backend_turnoApp.cobertura.entity;

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
@Table(name = "obra_social")
public class ObraSocial extends BaseEntity {

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String codigo;

	@Column(nullable = false)
	private boolean activo = true;

	public ObraSocial(String nombre, String codigo) {
		this.nombre = nombre;
		this.codigo = codigo;
	}
}
