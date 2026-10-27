package it.unife.volley.backend.domain.classifica;

/** Criteri di ordinamento disponibili, selezionabili dal client. */
public enum CriterioOrdinamento {
    /** Regolamento FIPAV: punti, vittorie, quoziente set, quoziente punti. */
    FIPAV,
    /** Prima le vittorie, poi i punti. */
    VITTORIE,
    /** Rendimento puro: quoziente set e quoziente punti. */
    QUOZIENTE_SET
}
