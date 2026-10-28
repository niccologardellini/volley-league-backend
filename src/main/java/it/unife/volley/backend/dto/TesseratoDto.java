package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.RuoloCampo;

/** Atleta tesserato restituito dalle API. */
public record TesseratoDto(Long id, String nome, String cognome, RuoloCampo ruoloCampo, Integer numeroMaglia,
                           Long idSquadra, String emailAccount) {
}
