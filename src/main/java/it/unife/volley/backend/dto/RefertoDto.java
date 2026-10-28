package it.unife.volley.backend.dto;

import java.util.List;

/** Referto completo mostrato al giudice per l'omologa: set, statistiche, liste gara e impronta SHA-256. */
public record RefertoDto(PartitaDto partita, List<SetDto> set, List<StatisticaDto> statistiche,
                         List<ListaGaraDto> liste, String note, String hashReferto) {
}
