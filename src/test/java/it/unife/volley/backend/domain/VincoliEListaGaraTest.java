package it.unife.volley.backend.domain;

import it.unife.volley.backend.domain.designazione.VincoliDesignazione;
import it.unife.volley.backend.domain.listagara.ListaGaraValidator;
import it.unife.volley.backend.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Vincoli di designazione arbitrale e regole di composizione della lista gara. */
class VincoliEListaGaraTest {

    private final VincoliDesignazione vincoli = new VincoliDesignazione();
    private final ListaGaraValidator listaValidator = new ListaGaraValidator();

    private final Societa ferrara = Societa.builder().id(1L).nome("Estense").provincia("FE").build();
    private final Societa bologna = Societa.builder().id(2L).nome("Due Torri").provincia("BO").build();
    private final Squadra casa = Squadra.builder().id(10L).nome("Estense Ferrara").societa(ferrara).build();
    private final Squadra ospite = Squadra.builder().id(20L).nome("Due Torri Bologna").societa(bologna).build();

    private Utente arbitro(String provincia, Societa societa) {
        return Utente.builder().id(99L).nome("A").cognome("B").ruolo(Ruolo.ARBITRO).provincia(provincia).societa(societa).build();
    }

    @Test
    void arbitroNeutraleDesignabile() {
        assertThat(vincoli.violazioni(arbitro("RE", null), casa, ospite, false)).isEmpty();
    }

    @Test
    void arbitroDellaStessaProvinciaNonDesignabile() {
        assertThat(vincoli.violazioni(arbitro("FE", null), casa, ospite, false))
                .singleElement().asString().contains("Stessa provincia");
    }

    @Test
    void arbitroAffiliatoAllaSocietaNonDesignabile() {
        Societa altraProvincia = Societa.builder().id(2L).nome("Due Torri").provincia("BO").build();
        assertThat(vincoli.violazioni(arbitro("MO", altraProvincia), casa, ospite, false))
                .anyMatch(m -> m.contains("Affiliato"));
    }

    @Test
    void arbitroGiaImpegnatoNelloStessoGiorno() {
        assertThat(vincoli.violazioni(arbitro("RE", null), casa, ospite, true)).anyMatch(m -> m.contains("stesso giorno"));
    }

    @Test
    void utenteNonArbitroRifiutato() {
        Utente dirigente = Utente.builder().id(5L).ruolo(Ruolo.DIRIGENTE).build();
        assertThat(vincoli.violazioni(dirigente, casa, ospite, false)).containsExactly("L'utente non e' un arbitro");
    }

    // ---- Lista gara ----

    private List<Tesserato> atleti(Squadra s, int n, int liberi) {
        List<Tesserato> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            out.add(Tesserato.builder().id((long) i + 1).nome("N" + i).cognome("C" + i).numeroMaglia(i + 1)
                    .ruoloCampo(i < liberi ? RuoloCampo.LIBERO : RuoloCampo.SCHIACCIATORE).squadra(s).build());
        }
        return out;
    }

    private List<Long> ids(List<Tesserato> t) {
        return t.stream().map(Tesserato::getId).toList();
    }

    @Test
    void listaDaQuattordiciConDueLiberiValida() {
        List<Tesserato> t = atleti(casa, 14, 2);
        assertThat(listaValidator.verifica(casa, ids(t), t)).isEmpty();
    }

    @Test
    void oltreQuattordiciAtletiNonAmmessi() {
        List<Tesserato> t = atleti(casa, 15, 2);
        assertThat(listaValidator.verifica(casa, ids(t), t)).anyMatch(m -> m.contains("Massimo 14"));
    }

    @Test
    void piuDiDodiciAtletiRichiedonoDueLiberi() {
        List<Tesserato> t = atleti(casa, 13, 1);
        assertThat(listaValidator.verifica(casa, ids(t), t)).anyMatch(m -> m.contains("obbligatori 2 liberi"));
    }

    @Test
    void troppiLiberi() {
        List<Tesserato> t = atleti(casa, 10, 3);
        assertThat(listaValidator.verifica(casa, ids(t), t)).anyMatch(m -> m.contains("Massimo 2 liberi"));
    }

    @Test
    void atletaDiAltraSquadraRifiutata() {
        List<Tesserato> t = atleti(casa, 8, 1);
        t.get(0).setSquadra(ospite);
        assertThat(listaValidator.verifica(casa, ids(t), t)).anyMatch(m -> m.contains("non e' tesserato/a"));
    }

    @Test
    void menoDiSeiAtleteNonAmmesse() {
        List<Tesserato> t = atleti(casa, 5, 0);
        assertThat(listaValidator.verifica(casa, ids(t), t)).anyMatch(m -> m.contains("almeno 6"));
    }
}
