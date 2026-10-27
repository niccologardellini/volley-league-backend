package it.unife.volley.backend.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Corpo JSON uniforme per tutte le risposte di errore delle API. */
public record ErrorResponse(int status, String messaggio, List<String> errori, LocalDateTime timestamp) {

    public static ErrorResponse of(int status, String messaggio, List<String> errori) {
        return new ErrorResponse(status, messaggio, errori, LocalDateTime.now());
    }
}
