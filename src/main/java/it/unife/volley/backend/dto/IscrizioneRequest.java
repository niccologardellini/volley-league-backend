package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Squadra da iscrivere a un campionato con iscrizioni aperte. */
public record IscrizioneRequest(@NotNull Long idSquadra) {
}
