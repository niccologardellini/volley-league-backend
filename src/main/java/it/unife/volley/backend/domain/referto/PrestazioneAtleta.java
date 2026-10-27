package it.unife.volley.backend.domain.referto;

import it.unife.volley.backend.model.Sanzione;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Statistiche individuali di un atleta inserite nel referto. */
public record PrestazioneAtleta(
        @NotNull Long idTesserato,
        @NotNull @Min(0) Integer puntiFatti,
        @NotNull @Min(0) Integer ace,
        @NotNull @Min(0) Integer muri,
        @NotNull @Min(0) Integer errori,
        Sanzione cartellino) {
}
