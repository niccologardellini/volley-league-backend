package it.unife.volley.backend.exception;

/** L'utente e' autenticato ma non e' proprietario della risorsa: tradotta in HTTP 403. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String messaggio) {
        super(messaggio);
    }
}
