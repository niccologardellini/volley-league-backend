package it.unife.volley.backend.domain;

import it.unife.volley.backend.domain.calendario.Accoppiamento;
import it.unife.volley.backend.domain.calendario.GeneratoreCalendario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Proprieta' del calendario all'italiana generato con l'algoritmo di Berger. */
class GeneratoreCalendarioTest {

    private final GeneratoreCalendario generatore = new GeneratoreCalendario();
    private static final LocalDate INIZIO = LocalDate.of(2026, 9, 26);

    private static List<String> squadre(int n) {
        return IntStream.rangeClosed(1, n).mapToObj(i -> "S" + i).toList();
    }

    @ParameterizedTest(name = "{0} squadre")
    @ValueSource(ints = {2, 4, 5, 6, 7, 10})
    void ogniCoppiaSiIncontraUnaVoltaPerGirone(int n) {
        List<Accoppiamento<String>> cal = generatore.genera(squadre(n), INIZIO, LocalTime.of(20, 30), 7, false);
        assertThat(cal).hasSize(n * (n - 1) / 2);
        Set<String> coppie = cal.stream().map(a -> {
            String[] x = {a.casa(), a.ospite()};
            Arrays.sort(x);
            return x[0] + "-" + x[1];
        }).collect(Collectors.toSet());
        assertThat(coppie).hasSize(n * (n - 1) / 2);
    }

    @Test
    void ogniSquadraGiocaAlPiuUnaVoltaPerGiornata() {
        List<Accoppiamento<String>> cal = generatore.genera(squadre(6), INIZIO, LocalTime.of(20, 30), 7, true);
        Map<Integer, List<Accoppiamento<String>>> perGiornata = cal.stream().collect(Collectors.groupingBy(Accoppiamento::giornata));
        assertThat(perGiornata).hasSize(10);
        perGiornata.values().forEach(g -> {
            List<String> presenti = g.stream().flatMap(a -> java.util.stream.Stream.of(a.casa(), a.ospite())).toList();
            assertThat(presenti).doesNotHaveDuplicates().hasSize(6);
        });
    }

    @Test
    void ritornoInverteIlCampo() {
        List<Accoppiamento<String>> cal = generatore.genera(squadre(4), INIZIO, LocalTime.of(20, 30), 7, true);
        Set<String> andata = cal.stream().filter(a -> a.giornata() <= 3).map(a -> a.casa() + ">" + a.ospite()).collect(Collectors.toSet());
        Set<String> ritorno = cal.stream().filter(a -> a.giornata() > 3).map(a -> a.ospite() + ">" + a.casa()).collect(Collectors.toSet());
        assertThat(ritorno).isEqualTo(andata);
    }

    @Test
    void conSquadreDispariUnaRiposaOgniGiornata() {
        List<Accoppiamento<String>> cal = generatore.genera(squadre(5), INIZIO, LocalTime.of(20, 30), 7, false);
        assertThat(cal.stream().map(Accoppiamento::giornata).distinct().count()).isEqualTo(5);
        assertThat(cal.stream().filter(a -> a.giornata() == 1)).hasSize(2);
    }

    @Test
    void dateCadenzateSettimanalmente() {
        List<Accoppiamento<String>> cal = generatore.genera(squadre(4), INIZIO, LocalTime.of(18, 0), 7, false);
        assertThat(cal.get(0).dataOra()).isEqualTo(INIZIO.atTime(18, 0));
        assertThat(cal.stream().filter(a -> a.giornata() == 3).findFirst().orElseThrow().dataOra())
                .isEqualTo(INIZIO.plusDays(14).atTime(18, 0));
    }

    @Test
    void servonoAlmenoDueSquadre() {
        assertThatThrownBy(() -> generatore.genera(squadre(1), INIZIO, LocalTime.NOON, 7, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
