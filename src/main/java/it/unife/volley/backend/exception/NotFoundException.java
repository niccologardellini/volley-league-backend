package it.unife.volley.backend.exception;

/** Risorsa inesistente: tradotta in HTTP 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String risorsa, Object id) {
        super(risorsa + " con id " + id + " non trovato/a");
    }
}
