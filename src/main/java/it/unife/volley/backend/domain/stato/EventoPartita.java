package it.unife.volley.backend.domain.stato;

/** Eventi che provocano una transizione di stato della partita. */
public enum EventoPartita {
    /** Entrambe le squadre hanno inviato la lista gara. */
    ROSTER_COMPLETATI,
    /** L'arbitro dichiara l'inizio della gara. */
    AVVIA_GARA,
    /** L'arbitro firma e invia il referto. */
    INVIA_REFERTO,
    /** Il giudice sportivo omologa il risultato. */
    OMOLOGA,
    /** Il giudice sportivo respinge il referto per correzione. */
    RESPINGI
}
