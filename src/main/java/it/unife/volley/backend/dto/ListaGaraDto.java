package it.unife.volley.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Lista gara inviata da una squadra: atleti convocati e staff in panchina. */
public record ListaGaraDto(Long id, Long idPartita, Long idSquadra, String squadra, List<TesseratoDto> atleti,
                           String allenatore, String viceAllenatore, String dirigenteAccompagnatore,
                           LocalDateTime inviataAt) {
}
