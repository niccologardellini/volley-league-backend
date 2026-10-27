package it.unife.volley.backend.exception;

import lombok.Getter;

import java.util.List;

/**
 * Violazione di una regola di business (es. referto non valido, lista gara non conforme).
 * Viene tradotta in HTTP 422 con l'elenco puntuale degli errori.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final List<String> errori;

    public BusinessException(String messaggio) {
        this(messaggio, List.of(messaggio));
    }

    public BusinessException(String messaggio, List<String> errori) {
        super(messaggio);
        this.errori = List.copyOf(errori);
    }
}
