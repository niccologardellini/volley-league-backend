package it.unife.volley.backend.domain.listagara;

import it.unife.volley.backend.model.RuoloCampo;
import it.unife.volley.backend.model.Squadra;
import it.unife.volley.backend.model.Tesserato;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Regole di composizione della lista gara (regolamento FIPAV):
 * <ul>
 *   <li>da 6 a 14 atleti, senza duplicati, tutti tesserati per la squadra;</li>
 *   <li>al massimo 2 liberi;</li>
 *   <li>se gli atleti a referto sono piu' di 12, i liberi devono essere esattamente 2.</li>
 * </ul>
 */
@Component
public class ListaGaraValidator {

    public static final int MIN_ATLETI = 6;
    public static final int MAX_ATLETI = 14;
    public static final int MAX_LIBERI = 2;
    public static final int SOGLIA_DOPPIO_LIBERO = 12;

    /** @return elenco delle violazioni; vuoto se la lista e' regolare */
    public List<String> verifica(Squadra squadra, List<Long> idRichiesti, List<Tesserato> trovati) {
        List<String> errori = new ArrayList<>();
        if (new HashSet<>(idRichiesti).size() != idRichiesti.size()) {
            errori.add("Un atleta e' stato inserito piu' volte");
        }
        if (trovati.size() != new HashSet<>(idRichiesti).size()) {
            errori.add("Uno o piu' atleti selezionati non esistono");
        }
        if (idRichiesti.size() < MIN_ATLETI) {
            errori.add("Servono almeno " + MIN_ATLETI + " atleti (selezionati: " + idRichiesti.size() + ")");
        }
        if (idRichiesti.size() > MAX_ATLETI) {
            errori.add("Massimo " + MAX_ATLETI + " atleti a referto (selezionati: " + idRichiesti.size() + ")");
        }
        for (Tesserato t : trovati) {
            if (!Objects.equals(t.getSquadra().getId(), squadra.getId())) {
                errori.add(t.getCognome() + " " + t.getNome() + " non e' tesserato/a per " + squadra.getNome());
            }
        }
        long liberi = trovati.stream().filter(t -> t.getRuoloCampo() == RuoloCampo.LIBERO).count();
        if (liberi > MAX_LIBERI) {
            errori.add("Massimo " + MAX_LIBERI + " liberi (selezionati: " + liberi + ")");
        }
        if (idRichiesti.size() > SOGLIA_DOPPIO_LIBERO && liberi != MAX_LIBERI) {
            errori.add("Con piu' di " + SOGLIA_DOPPIO_LIBERO + " atleti a referto sono obbligatori 2 liberi");
        }
        return errori;
    }
}
