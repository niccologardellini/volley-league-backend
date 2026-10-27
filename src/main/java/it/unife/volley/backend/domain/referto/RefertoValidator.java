package it.unife.volley.backend.domain.referto;

import it.unife.volley.backend.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Validatore formale del referto secondo le regole di gioco della pallavolo.
 * <ul>
 *   <li>Set 1-4: si vince a 25 con almeno 2 punti di scarto; oltre il 25 lo scarto deve essere esattamente 2 (es. 26-24, 31-29).</li>
 *   <li>Set 5 (tie-break): stessa regola con soglia 15.</li>
 *   <li>La partita termina quando una squadra vince 3 set: nessun set puo' essere giocato dopo.</li>
 *   <li>Massimo 6 sostituzioni per squadra per set.</li>
 * </ul>
 * La classe e' priva di stato e di dipendenze: e' testabile in isolamento (vedi RefertoValidatorTest).
 */
@Component
public class RefertoValidator {

    public static final int PUNTI_SET = 25;
    public static final int PUNTI_TIE_BREAK = 15;
    public static final int SCARTO_MINIMO = 2;
    public static final int SET_PER_VINCERE = 3;
    public static final int MAX_CAMBI_PER_SET = 6;

    /**
     * Valida l'insieme dei parziali e restituisce l'esito.
     *
     * @throws BusinessException con l'elenco di tutte le violazioni riscontrate
     */
    public EsitoPartita valida(List<ParzialeSet> parziali) {
        List<String> errori = verifica(parziali);
        if (!errori.isEmpty()) {
            throw new BusinessException("Referto non valido", errori);
        }
        return calcolaEsito(parziali);
    }

    /** Restituisce le violazioni senza lanciare eccezioni (lista vuota se il referto e' valido). */
    public List<String> verifica(List<ParzialeSet> parziali) {
        List<String> errori = new ArrayList<>();
        if (parziali == null || parziali.size() < SET_PER_VINCERE || parziali.size() > 2 * SET_PER_VINCERE - 1) {
            errori.add("Una partita deve avere da 3 a 5 set");
            return errori;
        }

        List<ParzialeSet> ordinati = parziali.stream()
                .sorted(Comparator.comparing(ParzialeSet::numeroSet))
                .toList();
        for (int i = 0; i < ordinati.size(); i++) {
            if (ordinati.get(i).numeroSet() != i + 1) {
                errori.add("I set devono essere numerati in sequenza da 1 a " + ordinati.size());
                return errori;
            }
        }

        int vintiCasa = 0;
        int vintiOspite = 0;
        for (ParzialeSet set : ordinati) {
            if (vintiCasa == SET_PER_VINCERE || vintiOspite == SET_PER_VINCERE) {
                errori.add("Set " + set.numeroSet() + ": la partita era gia' conclusa");
                break;
            }
            String erroreSet = verificaSet(set);
            if (erroreSet != null) {
                errori.add(erroreSet);
                continue;
            }
            if (set.puntiCasa() > set.puntiOspite()) {
                vintiCasa++;
            } else {
                vintiOspite++;
            }
        }

        if (errori.isEmpty() && vintiCasa != SET_PER_VINCERE && vintiOspite != SET_PER_VINCERE) {
            errori.add("Nessuna squadra ha raggiunto " + SET_PER_VINCERE + " set vinti (" + vintiCasa + "-" + vintiOspite + ")");
        }
        return errori;
    }

    /**
     * Verifica un singolo set.
     *
     * @return il messaggio di errore oppure {@code null} se il set e' regolare
     */
    public String verificaSet(ParzialeSet set) {
        int n = set.numeroSet();
        int soglia = n == 2 * SET_PER_VINCERE - 1 ? PUNTI_TIE_BREAK : PUNTI_SET;
        int vincente = Math.max(set.puntiCasa(), set.puntiOspite());
        int perdente = Math.min(set.puntiCasa(), set.puntiOspite());
        String etichetta = "Set " + n + " (" + set.puntiCasa() + "-" + set.puntiOspite() + ")";

        if (set.cambiCasa() > MAX_CAMBI_PER_SET || set.cambiOspite() > MAX_CAMBI_PER_SET) {
            return etichetta + ": massimo " + MAX_CAMBI_PER_SET + " sostituzioni per squadra";
        }
        if (vincente == perdente) {
            return etichetta + ": un set non puo' terminare in parita'";
        }
        if (vincente < soglia) {
            return etichetta + ": il set termina a " + soglia + " punti";
        }
        if (vincente - perdente < SCARTO_MINIMO) {
            return etichetta + ": servono almeno " + SCARTO_MINIMO + " punti di scarto";
        }
        if (vincente > soglia && vincente - perdente != SCARTO_MINIMO) {
            return etichetta + ": oltre i " + soglia + " punti il set termina con esattamente " + SCARTO_MINIMO + " punti di scarto";
        }
        return null;
    }

    /** Conta set e punti totali; da usare su parziali gia' validati. */
    public EsitoPartita calcolaEsito(List<ParzialeSet> parziali) {
        int sc = 0, so = 0, pc = 0, po = 0;
        for (ParzialeSet s : parziali) {
            pc += s.puntiCasa();
            po += s.puntiOspite();
            if (s.puntiCasa() > s.puntiOspite()) sc++;
            else so++;
        }
        return new EsitoPartita(sc, so, pc, po);
    }

    /** Coerenza interna delle statistiche individuali (ace e muri sono sottoinsiemi dei punti). */
    public List<String> verificaPrestazioni(List<PrestazioneAtleta> prestazioni) {
        List<String> errori = new ArrayList<>();
        if (prestazioni == null) return errori;
        long distinti = prestazioni.stream().map(PrestazioneAtleta::idTesserato).distinct().count();
        if (distinti != prestazioni.size()) {
            errori.add("Lo stesso atleta compare piu' volte nelle statistiche");
        }
        for (PrestazioneAtleta p : prestazioni) {
            if (p.ace() + p.muri() > p.puntiFatti()) {
                errori.add("Atleta " + p.idTesserato() + ": ace + muri non possono superare i punti fatti");
            }
        }
        return errori;
    }
}
