package it.unife.volley.backend.domain.designazione;

import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Squadra;
import it.unife.volley.backend.model.Utente;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Vincoli di compatibilita' per la designazione arbitrale:
 * <ol>
 *   <li>l'utente deve avere ruolo ARBITRO;</li>
 *   <li>l'arbitro non puo' essere affiliato alla societa' di una delle due squadre;</li>
 *   <li>l'arbitro non puo' risiedere nella provincia di una delle due societa';</li>
 *   <li>l'arbitro non puo' avere un'altra designazione nello stesso giorno.</li>
 * </ol>
 */
@Component
public class VincoliDesignazione {

    /** @return elenco delle violazioni; vuoto se l'arbitro e' designabile */
    public List<String> violazioni(Utente arbitro, Squadra casa, Squadra ospite, boolean giaImpegnato) {
        List<String> motivi = new ArrayList<>();
        if (arbitro.getRuolo() != Ruolo.ARBITRO) {
            motivi.add("L'utente non e' un arbitro");
            return motivi;
        }
        for (Squadra s : List.of(casa, ospite)) {
            if (arbitro.getSocieta() != null && Objects.equals(arbitro.getSocieta().getId(), s.getSocieta().getId())) {
                motivi.add("Affiliato alla societa' " + s.getSocieta().getNome());
            }
        }
        for (Squadra s : List.of(casa, ospite)) {
            String prov = s.getSocieta().getProvincia();
            if (arbitro.getProvincia() != null && arbitro.getProvincia().equalsIgnoreCase(prov)
                    && motivi.stream().noneMatch(m -> m.endsWith("(" + prov + ")"))) {
                motivi.add("Stessa provincia di " + s.getNome() + " (" + prov + ")");
            }
        }
        if (giaImpegnato) {
            motivi.add("Gia' designato per un'altra gara nello stesso giorno");
        }
        return motivi;
    }
}
