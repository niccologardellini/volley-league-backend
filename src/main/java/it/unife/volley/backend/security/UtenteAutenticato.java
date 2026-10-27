package it.unife.volley.backend.security;

import it.unife.volley.backend.model.Ruolo;

/** Principal ricavato dal token JWT: evita un accesso al DB per ogni richiesta. */
public record UtenteAutenticato(Long id, String email, Ruolo ruolo) {
}
