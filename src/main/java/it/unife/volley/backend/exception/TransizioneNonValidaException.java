package it.unife.volley.backend.exception;

import it.unife.volley.backend.model.StatoPartita;

/** Tentativo di transizione non ammessa dalla macchina a stati della partita: HTTP 409. */
public class TransizioneNonValidaException extends RuntimeException {

    public TransizioneNonValidaException(StatoPartita statoCorrente, String operazione) {
        super("Operazione '" + operazione + "' non consentita: la partita e' nello stato " + statoCorrente);
    }
}
