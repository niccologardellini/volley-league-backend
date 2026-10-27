package it.unife.volley.backend.domain;

import it.unife.volley.backend.domain.classifica.*;
import it.unife.volley.backend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Punteggio FIPAV, quozienti e strategie di ordinamento della classifica. */
class ClassificaTest {

    private final CalcolatoreClassifica calcolatore = new CalcolatoreClassifica();
    private Squadra a, b, c;
    private final List<Partita> partite = new ArrayList<>();
    private final List<SetPartita> set = new ArrayList<>();
    private long idPartita = 1;

    @BeforeEach
    void setUp() {
        Societa s = Societa.builder().id(1L).nome("Soc").provincia("FE").build();
        a = Squadra.builder().id(1L).nome("Alfa").societa(s).build();
        b = Squadra.builder().id(2L).nome("Beta").societa(s).build();
        c = Squadra.builder().id(3L).nome("Gamma").societa(s).build();
    }

    private void gioca(Squadra casa, Squadra ospite, String... parziali) {
        Partita p = Partita.builder().id(idPartita++).squadraCasa(casa).squadraOspite(ospite).stato(StatoPartita.OMOLOGATA).build();
        int sc = 0, so = 0;
        for (int i = 0; i < parziali.length; i++) {
            String[] x = parziali[i].split("-");
            int pc = Integer.parseInt(x[0]), po = Integer.parseInt(x[1]);
            if (pc > po) sc++; else so++;
            set.add(SetPartita.builder().partita(p).numeroSet(i + 1).puntiCasa(pc).puntiOspite(po).cambiCasa(0).cambiOspite(0).build());
        }
        p.setSetCasa(sc);
        p.setSetOspite(so);
        partite.add(p);
    }

    @ParameterizedTest(name = "{0}-{1} vale {2} punti")
    @CsvSource({"3,0,3", "3,1,3", "3,2,2", "2,3,1", "1,3,0", "0,3,0"})
    void regolaPunteggioFipav(int fatti, int subiti, int punti) {
        assertThat(RegolaPunteggio.punti(fatti, subiti)).isEqualTo(punti);
    }

    @Test
    void risultatoImpossibileRifiutato() {
        assertThatThrownBy(() -> RegolaPunteggio.punti(2, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tieBreakAssegnaDuePuntiAllaVincenteEUnoAllaPerdente() {
        gioca(a, b, "25-20", "20-25", "25-23", "22-25", "15-10");
        List<RigaClassifica> r = calcolatore.calcola(List.of(a, b), partite, set, new FipavStrategy());
        assertThat(r.get(0).getSquadra()).isEqualTo("Alfa");
        assertThat(r.get(0).getPunti()).isEqualTo(2);
        assertThat(r.get(1).getPunti()).isEqualTo(1);
        assertThat(r.get(0).getRisultati()[2]).isEqualTo(1); // un 3-2
        assertThat(r.get(1).getRisultati()[3]).isEqualTo(1); // un 2-3
    }

    @Test
    void quozientiSetEPunti() {
        gioca(a, b, "25-20", "25-20", "20-25", "25-20"); // Alfa 3-1, 95-85
        RigaClassifica alfa = calcolatore.calcola(List.of(a, b), partite, set, new FipavStrategy()).get(0);
        assertThat(alfa.getQuozienteSet()).isEqualTo(3.0);
        assertThat(alfa.getQuozientePunti()).isEqualTo(Math.round(95 * 1000.0 / 85) / 1000.0);
    }

    @Test
    void quozienteSetNulloQuandoNessunSetPerso() {
        gioca(a, b, "25-10", "25-10", "25-10");
        RigaClassifica alfa = calcolatore.calcola(List.of(a, b), partite, set, new FipavStrategy()).get(0);
        assertThat(alfa.getQuozienteSet()).isNull(); // "MAX" in interfaccia
    }

    @Test
    void aParitaDiPuntiEVittorieDecideIlQuozienteSet() {
        // Alfa e Beta: 1 vittoria 3-0 e 1 sconfitta ciascuna -> 3 punti; decide il quoziente set
        gioca(a, c, "25-20", "25-20", "25-20");             // Alfa 3-0
        gioca(b, c, "25-20", "20-25", "25-20", "25-20");    // Beta 3-1
        gioca(c, a, "25-20", "25-20", "25-20");             // Alfa 0-3
        gioca(c, b, "25-20", "25-20", "25-20");             // Beta 0-3
        List<RigaClassifica> r = calcolatore.calcola(List.of(a, b, c), partite, set, new FipavStrategy());
        RigaClassifica alfa = r.stream().filter(x -> x.getSquadra().equals("Alfa")).findFirst().orElseThrow();
        RigaClassifica beta = r.stream().filter(x -> x.getSquadra().equals("Beta")).findFirst().orElseThrow();
        assertThat(alfa.getPunti()).isEqualTo(beta.getPunti());
        assertThat(alfa.getVinte()).isEqualTo(beta.getVinte());
        // Alfa 3/3 = 1.0, Beta 3/4 = 0.75
        assertThat(alfa.getPosizione()).isLessThan(beta.getPosizione());
    }

    @Test
    void strategiaVittorieDiversaDaFipav() {
        // Alfa: due vittorie al tie-break (4 punti). Beta: una vittoria 3-0 e una sconfitta 2-3 (4 punti)...
        // Gamma: una vittoria 3-0 (3 punti) e una sconfitta. Con VITTORIE Alfa resta prima per numero di vittorie.
        gioca(a, b, "25-20", "20-25", "25-20", "20-25", "15-10");
        gioca(a, c, "25-20", "20-25", "25-20", "20-25", "15-10");
        gioca(b, c, "25-20", "25-20", "25-20");
        List<RigaClassifica> fipav = calcolatore.calcola(List.of(a, b, c), partite, set, new FipavStrategy());
        List<RigaClassifica> vittorie = calcolatore.calcola(List.of(a, b, c), partite, set, new VittorieStrategy());
        assertThat(vittorie.get(0).getSquadra()).isEqualTo("Alfa");
        assertThat(fipav.get(0).getPunti()).isEqualTo(4);
    }

    @Test
    void squadraSenzaPartiteCompareInClassifica() {
        List<RigaClassifica> r = calcolatore.calcola(List.of(a, b, c), List.of(), List.of(), new FipavStrategy());
        assertThat(r).hasSize(3).allMatch(x -> x.getGiocate() == 0 && x.getPunti() == 0);
        assertThat(r).extracting(RigaClassifica::getPosizione).containsExactly(1, 2, 3);
    }

    @Test
    void confrontoQuozientiConDenominatoreZero() {
        assertThat(Quozienti.confronta(3, 0, 10, 1)).isPositive();  // infinito > 10
        assertThat(Quozienti.confronta(0, 0, 1, 5)).isNegative();   // 0/0 trattato come 0
        assertThat(Quozienti.confronta(2, 4, 1, 2)).isZero();       // 0.5 == 0.5
    }
}
