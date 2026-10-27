package it.unife.volley.backend.domain.classifica;

/**
 * Assegnazione dei punti in classifica secondo il regolamento FIPAV:
 * <ul>
 *   <li>3-0 o 3-1: 3 punti alla vincente, 0 alla perdente;</li>
 *   <li>3-2: 2 punti alla vincente, 1 alla perdente.</li>
 * </ul>
 */
public final class RegolaPunteggio {

    private RegolaPunteggio() {
    }

    /** @return i punti in classifica spettanti alla squadra che ha vinto {@code setFatti} e perso {@code setSubiti} */
    public static int punti(int setFatti, int setSubiti) {
        if (setFatti == 3 && setSubiti <= 1) return 3;
        if (setFatti == 3 && setSubiti == 2) return 2;
        if (setFatti == 2 && setSubiti == 3) return 1;
        if (setFatti <= 1 && setSubiti == 3) return 0;
        throw new IllegalArgumentException("Risultato non valido: " + setFatti + "-" + setSubiti);
    }
}
