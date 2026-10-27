package it.unife.volley.backend.domain.classifica;

/**
 * Confronto esatto tra quozienti (a/b vs c/d) tramite prodotto incrociato,
 * per evitare errori di arrotondamento e gestire il denominatore nullo (quoziente infinito).
 */
public final class Quozienti {

    private Quozienti() {
    }

    /** @return valore negativo, zero o positivo come {@link java.util.Comparator#compare} (ordine crescente) */
    public static int confronta(int num1, int den1, int num2, int den2) {
        boolean inf1 = den1 == 0 && num1 > 0;
        boolean inf2 = den2 == 0 && num2 > 0;
        if (inf1 && inf2) return Integer.compare(num1, num2);
        if (inf1) return 1;
        if (inf2) return -1;
        long n1 = den1 == 0 ? 0 : num1;
        long d1 = den1 == 0 ? 1 : den1;
        long n2 = den2 == 0 ? 0 : num2;
        long d2 = den2 == 0 ? 1 : den2;
        return Long.compare(n1 * d2, n2 * d1);
    }
}
