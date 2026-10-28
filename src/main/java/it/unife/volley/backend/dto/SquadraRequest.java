package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Nome della nuova squadra creata dal dirigente per la propria societa'. */
public record SquadraRequest(@NotBlank @Size(max = 100) String nome) {
}
