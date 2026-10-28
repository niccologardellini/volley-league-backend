package it.unife.volley.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** Parametri per generare il calendario: data della prima giornata, orario, giorni tra due giornate e ritorno. */
public record GenerazioneCalendarioRequest(
        @NotNull LocalDate dataInizio,
        @NotNull LocalTime ora,
        @NotNull @Min(1) @Max(30) Integer giorniTraGiornate,
        boolean andataRitorno) {
}
