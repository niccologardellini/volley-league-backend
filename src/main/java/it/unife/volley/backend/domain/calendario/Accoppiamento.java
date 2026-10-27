package it.unife.volley.backend.domain.calendario;

import java.time.LocalDateTime;

/** Singolo incontro generato dall'algoritmo di calendario. */
public record Accoppiamento<T>(int giornata, T casa, T ospite, LocalDateTime dataOra) {
}
