package com.nicosgodoy.backend_turnoApp.usuario.entity;

import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import com.nicosgodoy.backend_turnoApp.shared.entity.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario extends BaseEntity {

	@Column(nullable = false)
	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rol rol;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String apellido;

	@Column(nullable = false)
	private boolean activo = true;

	public String nombreCompleto() {
		return (nombre + " " + apellido).trim();
	}
}
