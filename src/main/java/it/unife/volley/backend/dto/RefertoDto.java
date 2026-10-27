package it.unife.volley.backend.dto;

import java.util.List;

public record RefertoDto(PartitaDto partita, List<SetDto> set, List<StatisticaDto> statistiche,
                         List<ListaGaraDto> liste, String note, String hashReferto) {
}
