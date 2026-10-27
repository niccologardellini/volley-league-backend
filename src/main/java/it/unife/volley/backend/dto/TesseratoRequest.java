package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.RuoloCampo;
import jakarta.validation.constraints.*;

/** Se {@code emailAccount} e' valorizzata viene creato l'account GIOCATORE per l'area personale. */
public record TesseratoRequest(
        @NotBlank @Size(max = 60) String nome,
        @NotBlank @Size(max = 60) String cognome,
        @NotNull RuoloCampo ruoloCampo,
        @NotNull @Min(1) @Max(99) Integer numeroMaglia,
        @Email String emailAccount) {
}
