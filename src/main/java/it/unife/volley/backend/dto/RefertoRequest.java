package it.unife.volley.backend.dto;

import it.unife.volley.backend.domain.referto.ParzialeSet;
import it.unife.volley.backend.domain.referto.PrestazioneAtleta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Referto digitale: parziali, statistiche e password dell'arbitro usata come firma. */
public record RefertoRequest(
        @NotNull @Valid List<ParzialeSet> set,
        @NotNull @Valid List<PrestazioneAtleta> statistiche,
        @Size(max = 1000) String note,
        @NotBlank String passwordFirma) {
}
