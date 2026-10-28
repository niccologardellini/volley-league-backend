package it.unife.volley.backend.dto;

/** Risposta al login: token JWT da inviare nelle richieste successive e dati dell'utente. */
public record LoginResponse(String token, UtenteDto utente) {
}
