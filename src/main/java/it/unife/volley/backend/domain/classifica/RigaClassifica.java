package it.unife.volley.backend.domain.classifica;

import lombok.Getter;
import lombok.Setter;

/** Riga della classifica di una squadra, accumulata partita per partita. */
@Getter
public class RigaClassifica {

    @Setter
    private int posizione;
    private final Long idSquadra;
    private final String squadra;
    private final String societa;
    private int punti;
    private int giocate;
    private int vinte;
    private int perse;
    private int setVinti;
    private int setPersi;
    private int puntiFatti;
    private int puntiSubiti;
    /** Distribuzione dei risultati: 3-0, 3-1, 3-2, 2-3, 1-3, 0-3. */
    private final int[] risultati = new int[6];

    public RigaClassifica(Long idSquadra, String squadra, String societa) {
        this.idSquadra = idSquadra;
        this.squadra = squadra;
        this.societa = societa;
    }

    /** Registra una partita giocata dal punto di vista di questa squadra. */
    public void registra(int setFatti, int setSubiti, int puntiFattiPartita, int puntiSubitiPartita) {
        giocate++;
        if (setFatti > setSubiti) vinte++;
        else perse++;
        punti += RegolaPunteggio.punti(setFatti, setSubiti);
        setVinti += setFatti;
        setPersi += setSubiti;
        puntiFatti += puntiFattiPartita;
        puntiSubiti += puntiSubitiPartita;
        int indice = setFatti == 3 ? setSubiti : 5 - setFatti;
        risultati[indice]++;
    }

    /** Quoziente set; {@code null} se non ci sono set persi (quoziente "infinito"). */
    public Double getQuozienteSet() {
        return quoziente(setVinti, setPersi);
    }

    /** Quoziente punti; {@code null} se non ci sono punti subiti. */
    public Double getQuozientePunti() {
        return quoziente(puntiFatti, puntiSubiti);
    }

    private static Double quoziente(int num, int den) {
        if (den == 0) return num == 0 ? 0.0 : null;
        return Math.round(num * 1000.0 / den) / 1000.0;
    }
}
