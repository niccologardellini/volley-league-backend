package it.unife.volley.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivazione obbligatoria con cui il giudice respinge un referto. */
public record MotivazioneRequest(@NotBlank @Size(max = 1000) String motivazione) {
}
