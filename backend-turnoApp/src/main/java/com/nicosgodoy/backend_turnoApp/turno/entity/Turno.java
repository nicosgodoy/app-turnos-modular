package com.nicosgodoy.backend_turnoApp.turno.entity;

import com.nicosgodoy.backend_turnoApp.agenda.entity.Consultorio;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import com.nicosgodoy.backend_turnoApp.paciente.entity.Paciente;
import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "turno")
public class Turno extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "medico_id", nullable = false)
	private Medico medico;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "paciente_id", nullable = false)
	private Paciente paciente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "consultorio_id", nullable = false)
	private Consultorio consultorio;

	@Column(name = "fecha_hora", nullable = false)
	private OffsetDateTime fechaHora;

	@Column(name = "duracion_min", nullable = false)
	private int duracionMin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoTurno estado;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_cobertura", nullable = false)
	private TipoCobertura tipoCobertura;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "obra_social_id")
	private ObraSocial obraSocial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(name = "numero_afiliado")
	private String numeroAfiliado;

	private BigDecimal precio;

	private String motivo;

	@Column(nullable = false)
	private String canal = "WEB";

	@Column(name = "cancelado_motivo")
	private String canceladoMotivo;
}
