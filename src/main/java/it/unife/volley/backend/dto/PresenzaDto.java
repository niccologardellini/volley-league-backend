package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.Sanzione;
import it.unife.volley.backend.model.StatoPartita;

import java.time.LocalDateTime;

/** Riga dello storico di un giocatore: partita, risultato e statistiche personali. */
public record PresenzaDto(Long idPartita, LocalDateTime dataOra, String avversario, boolean inCasa,
                          StatoPartita stato, String risultato, Integer puntiFatti, Integer ace, Integer muri,
                          Integer errori, Sanzione cartellino) {
}
