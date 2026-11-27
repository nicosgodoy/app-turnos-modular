package com.nicosgodoy.backend_turnoApp.agenda.entity;

import com.nicosgodoy.backend_turnoApp.medico.entity.Medico;
import com.nicosgodoy.backend_turnoApp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "disponibilidad")
public class Disponibilidad extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "medico_id", nullable = false)
	private Medico medico;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "consultorio_id", nullable = false)
	private Consultorio consultorio;

	@Column(name = "dia_semana", nullable = false)
	private int diaSemana;

	@Column(name = "hora_desde", nullable = false)
	private LocalTime horaDesde;

	@Column(name = "hora_hasta", nullable = false)
	private LocalTime horaHasta;

	@Column(name = "vigente_desde", nullable = false)
	private LocalDate vigenteDesde;

	@Column(name = "vigente_hasta")
	private LocalDate vigenteHasta;

	@Column(nullable = false)
	private boolean activo = true;
}
