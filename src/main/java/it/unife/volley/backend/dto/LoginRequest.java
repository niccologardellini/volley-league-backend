package it.unife.volley.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenziali inviate dalla pagina di login. */
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
}
