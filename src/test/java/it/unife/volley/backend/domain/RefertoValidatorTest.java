package it.unife.volley.backend.domain;

import it.unife.volley.backend.domain.referto.EsitoPartita;
import it.unife.volley.backend.domain.referto.ParzialeSet;
import it.unife.volley.backend.domain.referto.PrestazioneAtleta;
import it.unife.volley.backend.domain.referto.RefertoValidator;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.model.Sanzione;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Test unitari delle regole formali del referto (punteggi set, tie-break, fine partita). */
class RefertoValidatorTest {

    private final RefertoValidator validator = new RefertoValidator();

    /** Costruisce i parziali da stringhe "25-20". */
    private static List<ParzialeSet> set(String... punteggi) {
        List<ParzialeSet> out = new ArrayList<>();
        for (int i = 0; i < punteggi.length; i++) {
            String[] p = punteggi[i].split("-");
            out.add(new ParzialeSet(i + 1, Integer.parseInt(p[0]), Integer.parseInt(p[1]), 0, 0));
        }
        return out;
    }

    @Nested
    @DisplayName("Singolo set")
    class SingoloSet {

        @ParameterizedTest(name = "set {0}: {1}-{2} e' regolare")
        @CsvSource({"1,25,23", "2,23,25", "1,26,24", "3,31,29", "1,25,0", "5,15,13", "5,17,15", "5,9,15"})
        void setRegolari(int numero, int casa, int ospite) {
            assertThat(validator.verificaSet(new ParzialeSet(numero, casa, ospite, 0, 0))).isNull();
        }

        @ParameterizedTest(name = "set {0}: {1}-{2} non e' regolare")
        @CsvSource({
                "1,25,24",  // scarto di 1
                "1,24,22",  // non raggiunge 25
                "1,27,24",  // ai vantaggi lo scarto deve essere esattamente 2
                "2,25,25",  // parita'
                "5,15,14",  // tie-break senza 2 punti di scarto
                "5,14,12",  // tie-break non raggiunge 15
                "5,25,20",  // tie-break oltre 15 con scarto > 2
                "4,30,25"   // vantaggi con scarto 5
        })
        void setIrregolari(int numero, int casa, int ospite) {
            assertThat(validator.verificaSet(new ParzialeSet(numero, casa, ospite, 0, 0))).isNotNull();
        }

        @Test
        void oltreSeiCambiNonAmmessi() {
            assertThat(validator.verificaSet(new ParzialeSet(1, 25, 20, 7, 0))).contains("sostituzioni");
        }
    }

    @Nested
    @DisplayName("Partita completa")
    class PartitaCompleta {

        @Test
        void vittoriaTreAZero() {
            EsitoPartita e = validator.valida(set("25-20", "25-18", "25-23"));
            assertThat(e.setCasa()).isEqualTo(3);
            assertThat(e.setOspite()).isZero();
            assertThat(e.puntiCasa()).isEqualTo(75);
            assertThat(e.puntiOspite()).isEqualTo(61);
        }

        @Test
        void vittoriaAlTieBreak() {
            EsitoPartita e = validator.valida(set("25-20", "20-25", "25-23", "22-25", "15-12"));
            assertThat(e.setCasa()).isEqualTo(3);
            assertThat(e.setOspite()).isEqualTo(2);
        }

        @Test
        void vittoriaOspiteTreAUno() {
            EsitoPartita e = validator.valida(set("25-20", "20-25", "23-25", "22-25"));
            assertThat(e.vinceCasa()).isFalse();
            assertThat(e.setOspite()).isEqualTo(3);
        }

        @Test
        void nessunaSquadraATreSet() {
            assertThatThrownBy(() -> validator.valida(set("25-20", "20-25", "25-23", "22-25")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrori())
                            .anyMatch(m -> m.contains("Nessuna squadra ha raggiunto 3 set")));
        }

        @Test
        void setGiocatoDopoLaFineDellaPartita() {
            assertThat(validator.verifica(set("25-20", "25-18", "25-23", "25-10")))
                    .anyMatch(m -> m.contains("gia' conclusa"));
        }

        @Test
        void menoDiTreSet() {
            assertThat(validator.verifica(set("25-20", "25-18"))).containsExactly("Una partita deve avere da 3 a 5 set");
        }

        @Test
        void quintoSetValutatoComeTieBreak() {
            // 25-20 e' regolare nei primi 4 set ma non al tie-break (oltre 15 lo scarto deve essere 2)
            assertThat(validator.verifica(set("25-20", "20-25", "25-23", "22-25", "25-20")))
                    .anyMatch(m -> m.startsWith("Set 5"));
        }

        @Test
        void numerazioneNonSequenziale() {
            List<ParzialeSet> s = List.of(new ParzialeSet(1, 25, 20, 0, 0), new ParzialeSet(2, 25, 20, 0, 0),
                    new ParzialeSet(4, 25, 20, 0, 0));
            assertThat(validator.verifica(s)).anyMatch(m -> m.contains("in sequenza"));
        }

        @Test
        void erroriMultipliRiportatiTutti() {
            List<String> errori = validator.verifica(set("25-24", "24-22", "25-20"));
            assertThat(errori).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void prestazioniIncoerenti() {
        var p = new PrestazioneAtleta(1L, 3, 2, 2, 0, Sanzione.NESSUNA);
        assertThat(validator.verificaPrestazioni(List.of(p))).anyMatch(m -> m.contains("ace + muri"));
    }

    @Test
    void atletaDuplicatoNelleStatistiche() {
        var p = new PrestazioneAtleta(1L, 5, 1, 1, 0, Sanzione.NESSUNA);
        assertThat(validator.verificaPrestazioni(List.of(p, p))).anyMatch(m -> m.contains("piu' volte"));
    }
}
