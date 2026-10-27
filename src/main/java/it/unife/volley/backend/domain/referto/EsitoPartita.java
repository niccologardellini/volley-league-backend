package it.unife.volley.backend.domain.referto;

/** Risultato sintetico di una partita ricavato dai parziali. */
public record EsitoPartita(int setCasa, int setOspite, int puntiCasa, int puntiOspite) {

    public boolean vinceCasa() {
        return setCasa > setOspite;
    }
}
