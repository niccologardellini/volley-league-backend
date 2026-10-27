package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.exception.TransizioneNonValidaException;
import it.unife.volley.backend.model.StatoPartita;

/**
 * State Pattern: ogni stato della partita e' un oggetto che decide quali eventi accettare
 * e verso quale stato transitare. Il comportamento di default e' rifiutare l'evento,
 * cosi' ogni stato concreto dichiara esplicitamente solo le transizioni ammesse.
 */
public interface StatoPartitaState {

    /** Stato rappresentato da questa implementazione. */
    StatoPartita stato();

    default StatoPartita rosterCompletati() {
        throw new TransizioneNonValidaException(stato(), "completamento liste gara");
    }

    default StatoPartita avviaGara() {
        throw new TransizioneNonValidaException(stato(), "avvio gara");
    }

    default StatoPartita inviaReferto() {
        throw new TransizioneNonValidaException(stato(), "invio referto");
    }

    default StatoPartita omologa() {
        throw new TransizioneNonValidaException(stato(), "omologazione");
    }

    default StatoPartita respingi() {
        throw new TransizioneNonValidaException(stato(), "respingimento referto");
    }

    /** Le liste gara si possono (re)inviare solo prima dell'inizio della gara. */
    default boolean listaGaraModificabile() {
        return false;
    }

    /** La designazione arbitrale e' modificabile solo prima dell'inizio della gara. */
    default boolean designazioneModificabile() {
        return false;
    }
}
