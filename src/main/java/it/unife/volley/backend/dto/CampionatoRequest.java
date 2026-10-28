package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dati per creare un campionato; la stagione deve avere il formato AAAA/AAAA. */
public record CampionatoRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 60) String categoria,
        @NotBlank @Pattern(regexp = "\\d{4}/\\d{4}", message = "formato atteso AAAA/AAAA") String stagione,
        @NotBlank @Size(max = 10) String girone) {
}
