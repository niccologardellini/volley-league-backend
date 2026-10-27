package it.unife.volley.backend.model;

/** Sanzioni disciplinari registrabili sul referto (regolamento FIPAV). */
public enum Sanzione {
    NESSUNA,
    /** Cartellino giallo: ammonizione. */
    GIALLO,
    /** Cartellino rosso: penalizzazione (punto all'avversario). */
    ROSSO,
    /** Rosso + giallo insieme: espulsione per il resto del set. */
    ROSSO_GIALLO
}
