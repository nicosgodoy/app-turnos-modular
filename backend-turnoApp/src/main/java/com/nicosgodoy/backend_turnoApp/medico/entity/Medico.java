package com.nicosgodoy.backend_turnoApp.medico.entity;

import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "medico")
public class Medico extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private Usuario usuario;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String apellido;

	@Column(nullable = false, unique = true)
	private String matricula;

	@Column(name = "duracion_turno_min", nullable = false)
	private int duracionTurnoMin = 30;

	@Column(name = "precio_particular", nullable = false)
	private BigDecimal precioParticular = BigDecimal.ZERO;

	private String biografia;

	@Column(nullable = false)
	private boolean activo = true;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "medico_especialidad",
			joinColumns = @JoinColumn(name = "medico_id"),
			inverseJoinColumns = @JoinColumn(name = "especialidad_id"))
	private Set<Especialidad> especialidades = new LinkedHashSet<>();

	public String nombreCompleto() {
		return (nombre + " " + apellido).trim();
	}
}
