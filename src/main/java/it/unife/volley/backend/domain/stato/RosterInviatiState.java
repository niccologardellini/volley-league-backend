package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.model.StatoPartita;

/** Entrambe le liste gara sono state inviate: l'arbitro puo' avviare la gara. */
public class RosterInviatiState implements StatoPartitaState {

    @Override
    public StatoPartita stato() {
        return StatoPartita.ROSTER_INVIATI;
    }

    /** Una squadra che corregge la propria lista non fa regredire lo stato. */
    @Override
    public StatoPartita rosterCompletati() {
        return StatoPartita.ROSTER_INVIATI;
    }

    @Override
    public StatoPartita avviaGara() {
        return StatoPartita.IN_CORSO;
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
