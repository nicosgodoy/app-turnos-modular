package com.nicosgodoy.backend_turnoApp.agenda.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface ReservasPort {

	Set<LocalDateTime> fechasOcupadas(UUID medicoId, LocalDate desde, LocalDate hasta);
}
