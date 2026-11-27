package com.nicosgodoy.backend_turnoApp.paciente.entity;

import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import com.nicosgodoy.backend_turnoApp.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "paciente")
public class Paciente extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", unique = true)
	private Usuario usuario;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String apellido;

	@Column(nullable = false, unique = true)
	private String dni;

	private LocalDate fechaNacimiento;

	@Column(nullable = false)
	private String telefono;

	@Column(nullable = false)
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "obra_social_id")
	private ObraSocial obraSocial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(name = "numero_afiliado")
	private String numeroAfiliado;

	@Column(name = "consentimiento_datos", nullable = false)
	private boolean consentimientoDatos;
}
