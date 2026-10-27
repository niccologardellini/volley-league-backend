package it.unife.volley.backend.domain;

import it.unife.volley.backend.domain.stato.EventoPartita;
import it.unife.volley.backend.domain.stato.MacchinaStatiPartita;
import it.unife.volley.backend.exception.TransizioneNonValidaException;
import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.StatoPartita;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Verifica di tutte le transizioni ammesse e di alcune vietate dello State Pattern. */
class MacchinaStatiPartitaTest {

    private final MacchinaStatiPartita macchina = new MacchinaStatiPartita();

    private static Partita in(StatoPartita stato) {
        return Partita.builder().stato(stato).build();
    }

    @Test
    void cicloDiVitaCompleto() {
        Partita p = in(StatoPartita.PROGRAMMATA);
        macchina.applica(p, EventoPartita.ROSTER_COMPLETATI);
        assertThat(p.getStato()).isEqualTo(StatoPartita.ROSTER_INVIATI);
        macchina.applica(p, EventoPartita.AVVIA_GARA);
        assertThat(p.getStato()).isEqualTo(StatoPartita.IN_CORSO);
        macchina.applica(p, EventoPartita.INVIA_REFERTO);
        assertThat(p.getStato()).isEqualTo(StatoPartita.REFERTATA);
        macchina.applica(p, EventoPartita.OMOLOGA);
        assertThat(p.getStato()).isEqualTo(StatoPartita.OMOLOGATA);
    }

    @Test
    void respingimentoRiportaInCorso() {
        Partita p = in(StatoPartita.REFERTATA);
        macchina.applica(p, EventoPartita.RESPINGI);
        assertThat(p.getStato()).isEqualTo(StatoPartita.IN_CORSO);
    }

    @ParameterizedTest(name = "{1} non ammesso in {0}")
    @CsvSource({
            "PROGRAMMATA,AVVIA_GARA", "PROGRAMMATA,INVIA_REFERTO", "ROSTER_INVIATI,OMOLOGA",
            "IN_CORSO,OMOLOGA", "IN_CORSO,ROSTER_COMPLETATI", "OMOLOGATA,RESPINGI", "OMOLOGATA,INVIA_REFERTO"})
    void transizioniVietate(StatoPartita stato, EventoPartita evento) {
        Partita p = in(stato);
        assertThatThrownBy(() -> macchina.applica(p, evento)).isInstanceOf(TransizioneNonValidaException.class);
        assertThat(p.getStato()).isEqualTo(stato);
    }

    @Test
    void listaGaraModificabileSoloPrimaDellaGara() {
        assertThat(macchina.statoDi(in(StatoPartita.PROGRAMMATA)).listaGaraModificabile()).isTrue();
        assertThat(macchina.statoDi(in(StatoPartita.ROSTER_INVIATI)).listaGaraModificabile()).isTrue();
        assertThat(macchina.statoDi(in(StatoPartita.IN_CORSO)).listaGaraModificabile()).isFalse();
        assertThat(macchina.statoDi(in(StatoPartita.OMOLOGATA)).listaGaraModificabile()).isFalse();
    }
}
