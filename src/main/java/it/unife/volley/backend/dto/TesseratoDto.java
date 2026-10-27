package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.RuoloCampo;

public record TesseratoDto(Long id, String nome, String cognome, RuoloCampo ruoloCampo, Integer numeroMaglia,
                           Long idSquadra, String emailAccount) {
}
