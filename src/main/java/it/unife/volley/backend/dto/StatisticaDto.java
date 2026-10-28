package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.Sanzione;

/** Statistiche di un atleta in una partita: punti, ace, muri, errori e cartellino. */
public record StatisticaDto(Long idTesserato, String atleta, Integer numeroMaglia, Long idSquadra,
                            Integer puntiFatti, Integer ace, Integer muri, Integer errori, Sanzione cartellino) {
}
