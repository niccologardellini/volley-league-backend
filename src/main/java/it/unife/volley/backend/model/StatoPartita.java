package it.unife.volley.backend.model;

/**
 * Stati del ciclo di vita di una partita.
 * Le transizioni ammesse sono governate dallo State Pattern in
 * {@link it.unife.volley.backend.domain.stato}.
 * <pre>
 * PROGRAMMATA -> ROSTER_INVIATI -> IN_CORSO -> REFERTATA -> OMOLOGATA
 *                                     ^             |
 *                                     +-- respinto -+
 * </pre>
 */
public enum StatoPartita {
    PROGRAMMATA,
    ROSTER_INVIATI,
    IN_CORSO,
    REFERTATA,
    OMOLOGATA
}
