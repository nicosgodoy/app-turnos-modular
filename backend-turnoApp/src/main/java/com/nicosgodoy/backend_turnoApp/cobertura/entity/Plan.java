package com.nicosgodoy.backend_turnoApp.cobertura.entity;

import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plan")
public class Plan extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "obra_social_id", nullable = false)
	private ObraSocial obraSocial;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String codigo;

	@Column(nullable = false)
	private boolean activo = true;

	public Plan(ObraSocial obraSocial, String nombre, String codigo) {
		this.obraSocial = obraSocial;
		this.nombre = nombre;
		this.codigo = codigo;
	}
}
