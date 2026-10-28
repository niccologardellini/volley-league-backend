package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.StatoPartita;

import java.time.LocalDateTime;
import java.util.List;

/** Partita del calendario con stato, squadre, arbitro designato e risultato set per set. */
public record PartitaDto(Long id, Long idCampionato, String campionato, Integer giornata, LocalDateTime dataOra,
                         StatoPartita stato, SquadraRef casa, SquadraRef ospite,
                         Long idArbitro, String arbitro, Integer setCasa, Integer setOspite,
                         List<String> parziali, LocalDateTime firmaArbitroAt, String noteGiudice) {
}
