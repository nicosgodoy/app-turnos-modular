package com.nicosgodoy.backend_turnoApp.medico.entity;

import com.nicosgodoy.backend_turnoApp.cobertura.entity.ObraSocial;
import com.nicosgodoy.backend_turnoApp.cobertura.entity.Plan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "medico_cobertura")
public class MedicoCobertura {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "medico_id", nullable = false)
	private Medico medico;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "obra_social_id", nullable = false)
	private ObraSocial obraSocial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id")
	private Plan plan;

	@Column(name = "creado_en", nullable = false, updatable = false)
	private Instant creadoEn;

	public MedicoCobertura(Medico medico, ObraSocial obraSocial, Plan plan) {
		this.medico = medico;
		this.obraSocial = obraSocial;
		this.plan = plan;
	}

	@PrePersist
	void prePersist() {
		if (this.creadoEn == null) {
			this.creadoEn = Instant.now();
		}
	}
}
