package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SquadraRequest(@NotBlank @Size(max = 100) String nome) {
}
