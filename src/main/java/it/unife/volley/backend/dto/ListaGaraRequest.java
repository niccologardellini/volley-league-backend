package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Lista gara compilata dal dirigente: id degli atleti convocati e nomi dello staff. */
public record ListaGaraRequest(
        @NotNull List<Long> atleti,
        @NotBlank @Size(max = 120) String allenatore,
        @Size(max = 120) String viceAllenatore,
        @Size(max = 120) String dirigenteAccompagnatore) {
}
