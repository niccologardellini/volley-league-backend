package it.unife.volley.backend.dto;

import java.util.List;

/** Arbitro proposto per una designazione, con l'esito dei vincoli (societa', provincia, stesso giorno) e gli eventuali motivi di esclusione. */
public record ArbitroCompatibileDto(Long id, String nome, String societa, String provincia,
                                    boolean compatibile, List<String> motivi) {
}
