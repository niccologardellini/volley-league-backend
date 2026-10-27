package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.model.StatoPartita;

/** Partita in calendario: le squadre stanno preparando le liste gara. */
public class ProgrammataState implements StatoPartitaState {

    @Override
    public StatoPartita stato() {
        return StatoPartita.PROGRAMMATA;
    }

    @Override
    public StatoPartita rosterCompletati() {
        return StatoPartita.ROSTER_INVIATI;
    }

    @Override
    public boolean listaGaraModificabile() {
        return true;
    }

    @Override
    public boolean designazioneModificabile() {
        return true;
    }
}
