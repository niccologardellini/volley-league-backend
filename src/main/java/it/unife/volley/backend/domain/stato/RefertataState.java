package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.model.StatoPartita;

/** Referto firmato e in attesa di revisione del giudice sportivo. */
public class RefertataState implements StatoPartitaState {

    @Override
    public StatoPartita stato() {
        return StatoPartita.REFERTATA;
    }

    @Override
    public StatoPartita omologa() {
        return StatoPartita.OMOLOGATA;
    }

    /** Il referto torna all'arbitro, che puo' correggerlo e rifirmarlo. */
    @Override
    public StatoPartita respingi() {
        return StatoPartita.IN_CORSO;
    }
}
