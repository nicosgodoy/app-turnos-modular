package com.nicosgodoy.backend_turnoApp.medico.dto;

import java.util.UUID;

public record CoberturaResponse(UUID obraSocialId, String obraSocial, UUID planId, String plan) {
}
