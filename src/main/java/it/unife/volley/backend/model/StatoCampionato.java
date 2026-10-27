package it.unife.volley.backend.model;

/** Stato organizzativo di un campionato. */
public enum StatoCampionato {
    /** Le societa' possono iscrivere le proprie squadre. */
    ISCRIZIONI_APERTE,
    /** Il calendario e' stato generato: iscrizioni chiuse. */
    IN_CORSO,
    /** Tutte le partite sono omologate. */
    CONCLUSO
}
