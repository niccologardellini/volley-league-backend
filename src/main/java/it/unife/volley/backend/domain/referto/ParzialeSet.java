package it.unife.volley.backend.domain.referto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Punteggio di un set cosi' come inserito dall'arbitro nel referto. */
public record ParzialeSet(
        @NotNull @Min(1) @Max(5) Integer numeroSet,
        @NotNull @Min(0) Integer puntiCasa,
        @NotNull @Min(0) Integer puntiOspite,
        @NotNull @Min(0) Integer cambiCasa,
        @NotNull @Min(0) Integer cambiOspite) {
}
