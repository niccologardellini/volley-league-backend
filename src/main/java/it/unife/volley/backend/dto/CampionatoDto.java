package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.StatoCampionato;

/** Campionato restituito dalle API, con numero di squadre iscritte e di partite generate. */
public record CampionatoDto(Long id, String nome, String categoria, String stagione, String girone,
                            StatoCampionato stato, long numeroSquadre, long numeroPartite) {
}
