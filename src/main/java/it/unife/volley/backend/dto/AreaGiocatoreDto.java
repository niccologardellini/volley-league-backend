package it.unife.volley.backend.dto;

import java.util.List;

/** Dati dell'area personale del giocatore: squadra, totali stagionali, storico presenze e prossime partite. */
public record AreaGiocatoreDto(TesseratoDto atleta, SquadraRef squadra, String campionato,
                               Totali totali, List<PresenzaDto> storico, List<PartitaDto> calendario) {

    public record Totali(long convocazioni, long presenze, long punti, long ace, long muri, long errori, double mediaPunti) {
    }
}
