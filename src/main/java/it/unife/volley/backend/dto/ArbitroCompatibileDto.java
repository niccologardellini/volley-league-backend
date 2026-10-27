package it.unife.volley.backend.dto;

import java.util.List;

public record ArbitroCompatibileDto(Long id, String nome, String societa, String provincia,
                                    boolean compatibile, List<String> motivi) {
}
