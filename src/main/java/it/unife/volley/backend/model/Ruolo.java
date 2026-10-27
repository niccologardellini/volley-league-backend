package it.unife.volley.backend.model;

/**
 * Ruoli applicativi usati per il controllo degli accessi (RBAC).
 * Il nome viene propagato nel token JWT e mappato sull'authority Spring "ROLE_&lt;nome&gt;".
 */
public enum Ruolo {
    /** Organizzatore / Giudice Sportivo: crea campionati, designa arbitri, omologa referti. */
    ADMIN,
    /** Presidente di societa' / Dirigente: gestisce squadre, tesseramenti e liste gara. */
    DIRIGENTE,
    /** Arbitro: consulta le designazioni e compila il referto digitale. */
    ARBITRO,
    /** Giocatore tesserato: consulta statistiche personali e calendario. */
    GIOCATORE
}
