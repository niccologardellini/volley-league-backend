package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.Ruolo;

public record UtenteDto(Long id, String email, String nome, String cognome, Ruolo ruolo,
                        Long idSocieta, String nomeSocieta, String provincia) {
}
