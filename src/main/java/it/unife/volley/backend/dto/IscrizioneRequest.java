package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotNull;

public record IscrizioneRequest(@NotNull Long idSquadra) {
}
