package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Arbitro scelto dal giudice sportivo per dirigere una partita. */
public record DesignazioneRequest(@NotNull Long idArbitro) {
}
