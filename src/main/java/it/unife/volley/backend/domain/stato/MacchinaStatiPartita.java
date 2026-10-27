package it.unife.volley.backend.domain.stato;

import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.StatoPartita;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Contesto dello State Pattern: risolve l'oggetto stato corrispondente allo stato
 * persistito della partita e applica l'evento richiesto.
 */
@Component
public class MacchinaStatiPartita {

    private final Map<StatoPartita, StatoPartitaState> stati = new EnumMap<>(StatoPartita.class);

    public MacchinaStatiPartita() {
        registra(new ProgrammataState());
        registra(new RosterInviatiState());
        registra(new InCorsoState());
        registra(new RefertataState());
        registra(new OmologataState());
    }

    private void registra(StatoPartitaState s) {
        stati.put(s.stato(), s);
    }

    public StatoPartitaState statoDi(Partita partita) {
        return stati.get(partita.getStato());
    }

    /**
     * Applica l'evento e aggiorna lo stato della partita.
     *
     * @throws it.unife.volley.backend.exception.TransizioneNonValidaException se l'evento non e' ammesso
     */
    public StatoPartita applica(Partita partita, EventoPartita evento) {
        StatoPartitaState corrente = statoDi(partita);
        StatoPartita nuovo = switch (evento) {
            case ROSTER_COMPLETATI -> corrente.rosterCompletati();
            case AVVIA_GARA -> corrente.avviaGara();
            case INVIA_REFERTO -> corrente.inviaReferto();
            case OMOLOGA -> corrente.omologa();
            case RESPINGI -> corrente.respingi();
        };
        partita.setStato(nuovo);
        return nuovo;
    }
}
