package it.unife.volley.backend.domain.classifica;

import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.SetPartita;
import it.unife.volley.backend.model.Squadra;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Calcola la classifica a partire dalle sole partite omologate.
 * Il criterio di ordinamento viene iniettato come strategia.
 */
@Component
public class CalcolatoreClassifica {

    /**
     * @param squadre iscritte al campionato (compaiono anche con 0 partite giocate)
     * @param partite partite omologate con {@code setCasa}/{@code setOspite} valorizzati
     * @param set     parziali delle partite, usati per i punti fatti/subiti
     */
    public List<RigaClassifica> calcola(List<Squadra> squadre, List<Partita> partite, List<SetPartita> set,
                                        OrdinamentoClassificaStrategy strategia) {
        Map<Long, RigaClassifica> righe = new LinkedHashMap<>();
        for (Squadra s : squadre) {
            righe.put(s.getId(), new RigaClassifica(s.getId(), s.getNome(), s.getSocieta().getNome()));
        }

        Map<Long, int[]> puntiPerPartita = new HashMap<>();
        for (SetPartita sp : set) {
            int[] tot = puntiPerPartita.computeIfAbsent(sp.getPartita().getId(), k -> new int[2]);
            tot[0] += sp.getPuntiCasa();
            tot[1] += sp.getPuntiOspite();
        }

        for (Partita p : partite) {
            int[] tot = puntiPerPartita.getOrDefault(p.getId(), new int[2]);
            RigaClassifica casa = righe.get(p.getSquadraCasa().getId());
            RigaClassifica ospite = righe.get(p.getSquadraOspite().getId());
            if (casa == null || ospite == null) continue;
            casa.registra(p.getSetCasa(), p.getSetOspite(), tot[0], tot[1]);
            ospite.registra(p.getSetOspite(), p.getSetCasa(), tot[1], tot[0]);
        }

        List<RigaClassifica> ordinata = new ArrayList<>(righe.values());
        ordinata.sort(strategia.comparatore());
        for (int i = 0; i < ordinata.size(); i++) {
            ordinata.get(i).setPosizione(i + 1);
        }
        return ordinata;
    }
}
