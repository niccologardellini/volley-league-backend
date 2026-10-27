package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.StatoCampionato;

public record CampionatoDto(Long id, String nome, String categoria, String stagione, String girone,
                            StatoCampionato stato, long numeroSquadre, long numeroPartite) {
}
