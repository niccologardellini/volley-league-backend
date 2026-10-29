package it.unife.volley.backend.domain;

import it.unife.volley.backend.domain.referto.EsitoPartita;
import it.unife.volley.backend.domain.referto.ParzialeSet;
import it.unife.volley.backend.domain.referto.RefertoValidator;
import it.unife.volley.backend.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Caso di test della validazione del referto: regole dei set e risultato finale della partita. */
class RefertoValidatorTest {

    private final RefertoValidator validator = new RefertoValidator();

    @Test
    void refertoRegolareVieneAccettato() {
        List<ParzialeSet> set = List.of(
                new ParzialeSet(1, 25, 20, 0, 0),
                new ParzialeSet(2, 25, 18, 0, 0),
                new ParzialeSet(3, 25, 23, 0, 0));

        EsitoPartita esito = validator.valida(set);

        assertThat(esito.setCasa()).isEqualTo(3);
        assertThat(esito.setOspite()).isZero();
    }

    @Test
    void setSenzaDuePuntiDiScartoVieneRifiutato() {
        // 25-24 non chiude il set: servono 2 punti di scarto
        List<ParzialeSet> set = List.of(
                new ParzialeSet(1, 25, 24, 0, 0),
                new ParzialeSet(2, 25, 18, 0, 0),
                new ParzialeSet(3, 25, 23, 0, 0));

        assertThatThrownBy(() -> validator.valida(set)).isInstanceOf(BusinessException.class);
    }
}
