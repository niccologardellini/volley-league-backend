package it.unife.volley.backend.dto;

/** Squadra restituita dalle API, con societa', campionato di iscrizione e numero di tesserati. */
public record SquadraDto(Long id, String nome, Long idSocieta, String nomeSocieta, String citta, String provincia,
                         Long idCampionato, String nomeCampionato, long numeroTesserati) {
}
