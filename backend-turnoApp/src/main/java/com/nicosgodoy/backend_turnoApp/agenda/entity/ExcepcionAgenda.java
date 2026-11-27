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
@Table(name = "excepcion_agenda")
public class ExcepcionAgenda extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "medico_id", nullable = false)
	private Medico medico;

	@Column(name = "fecha_desde", nullable = false)
	private LocalDate fechaDesde;

	@Column(name = "fecha_hasta", nullable = false)
	private LocalDate fechaHasta;

	@Column(name = "todo_el_dia", nullable = false)
	private boolean todoElDia = true;

	@Column(name = "hora_desde")
	private LocalTime horaDesde;

	@Column(name = "hora_hasta")
	private LocalTime horaHasta;

	private String motivo;
}
