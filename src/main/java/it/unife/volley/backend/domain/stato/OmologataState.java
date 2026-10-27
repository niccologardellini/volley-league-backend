package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.model.StatoPartita;

/** Stato finale: il risultato e' ufficiale e concorre alla classifica. Nessuna transizione ammessa. */
public class OmologataState implements StatoPartitaState {

    @Override
    public StatoPartita stato() {
        return StatoPartita.OMOLOGATA;
    }
}
