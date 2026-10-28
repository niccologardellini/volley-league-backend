package it.unife.volley.backend.dto;

/** Punteggio e cambi di un singolo set. */
public record SetDto(Integer numeroSet, Integer puntiCasa, Integer puntiOspite, Integer cambiCasa, Integer cambiOspite) {
}
