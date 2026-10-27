package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.model.StatoPartita;

/** Gara in svolgimento: l'arbitro compila e firma il referto. */
public class InCorsoState implements StatoPartitaState {

    @Override
    public StatoPartita stato() {
        return StatoPartita.IN_CORSO;
    }

    @Override
    public StatoPartita inviaReferto() {
        return StatoPartita.REFERTATA;
    }
}
