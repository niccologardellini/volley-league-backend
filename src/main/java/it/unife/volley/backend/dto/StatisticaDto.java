package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.Sanzione;

public record StatisticaDto(Long idTesserato, String atleta, Integer numeroMaglia, Long idSquadra,
                            Integer puntiFatti, Integer ace, Integer muri, Integer errori, Sanzione cartellino) {
}
