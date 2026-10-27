package it.unife.volley.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unife.volley.backend.domain.referto.ParzialeSet;
import it.unife.volley.backend.domain.referto.PrestazioneAtleta;
import it.unife.volley.backend.dto.RefertoRequest;
import it.unife.volley.backend.model.*;
import it.unife.volley.backend.repository.*;
import it.unife.volley.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test di integrazione (Spring Boot + H2 + MockMvc) sul salvataggio atomico del referto.
 * La classe NON e' @Transactional: ogni richiesta HTTP gira nella propria transazione,
 * cosi' si puo' verificare sul DB l'effettivo rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RefertoTransazionaleIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JwtService jwtService;
    @Autowired PasswordEncoder encoder;
    @Autowired UtenteRepository utenti;
    @Autowired SocietaRepository societa;
    @Autowired CampionatoRepository campionati;
    @Autowired SquadraRepository squadre;
    @Autowired TesseratoRepository tesserati;
    @Autowired PartitaRepository partite;
    @Autowired SetPartitaRepository setRepo;
    @Autowired ListaGaraRepository liste;
    @Autowired StatisticaGiocatoreRepository statistiche;

    private Utente arbitro, admin;
    private Partita partita;
    private List<Tesserato> rosaCasa, rosaOspite;
    private Tesserato nonConvocata;

    @BeforeEach
    void fixture() {
        statistiche.deleteAll();
        liste.deleteAll();
        partite.deleteAll();
        tesserati.deleteAll();
        squadre.deleteAll();
        campionati.deleteAll();
        utenti.deleteAll();
        societa.deleteAll();

        Societa fe = societa.save(Societa.builder().nome("Estense").codiceFipav("FE-1").citta("Ferrara").provincia("FE").build());
        Societa bo = societa.save(Societa.builder().nome("Due Torri").codiceFipav("BO-1").citta("Bologna").provincia("BO").build());
        arbitro = utenti.save(Utente.builder().email("arbitro@test.it").password(encoder.encode("password"))
                .nome("Luca").cognome("Rossi").ruolo(Ruolo.ARBITRO).provincia("RE").build());
        admin = utenti.save(Utente.builder().email("admin@test.it").password(encoder.encode("password"))
                .nome("Giulia").cognome("Ferri").ruolo(Ruolo.ADMIN).build());
        Campionato c = campionati.save(Campionato.builder().nome("Serie C").categoria("C").stagione("2026/2027")
                .girone("A").stato(StatoCampionato.IN_CORSO).build());
        Squadra casa = squadre.save(Squadra.builder().nome("Estense Ferrara").societa(fe).campionato(c).build());
        Squadra ospite = squadre.save(Squadra.builder().nome("Due Torri Bologna").societa(bo).campionato(c).build());
        rosaCasa = rosa(casa, 7);
        rosaOspite = rosa(ospite, 6);
        nonConvocata = rosaCasa.remove(6); // tesserata ma esclusa dalla lista gara

        partita = partite.save(Partita.builder().campionato(c).giornata(1).squadraCasa(casa).squadraOspite(ospite)
                .arbitro(arbitro).dataOra(LocalDateTime.of(2026, 10, 24, 20, 30)).stato(StatoPartita.IN_CORSO).build());
        liste.save(ListaGara.builder().partita(partita).squadra(casa).atleti(new LinkedHashSet<>(rosaCasa))
                .allenatore("All. Casa").inviataAt(LocalDateTime.now()).build());
        liste.save(ListaGara.builder().partita(partita).squadra(ospite).atleti(new LinkedHashSet<>(rosaOspite))
                .allenatore("All. Ospite").inviataAt(LocalDateTime.now()).build());
    }

    private List<Tesserato> rosa(Squadra s, int n) {
        List<Tesserato> out = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            out.add(tesserati.save(Tesserato.builder().nome("N" + i).cognome(s.getNome() + i).numeroMaglia(i)
                    .ruoloCampo(RuoloCampo.SCHIACCIATORE).squadra(s).build()));
        }
        return out;
    }

    private String bearer(Utente u) {
        return "Bearer " + jwtService.genera(u);
    }

    private List<ParzialeSet> treAUno() {
        return List.of(new ParzialeSet(1, 25, 20, 2, 3), new ParzialeSet(2, 23, 25, 1, 0),
                new ParzialeSet(3, 25, 18, 0, 2), new ParzialeSet(4, 26, 24, 4, 4));
    }

    private PrestazioneAtleta prest(Tesserato t, int punti) {
        return new PrestazioneAtleta(t.getId(), punti, 1, 1, 2, Sanzione.NESSUNA);
    }

    @Test
    void refertoValidoSalvatoEPartitaRefertata() throws Exception {
        var req = new RefertoRequest(treAUno(), List.of(prest(rosaCasa.get(0), 18), prest(rosaOspite.get(0), 12)), "Ok", "password");

        mvc.perform(post("/api/partite/{id}/referto", partita.getId()).header("Authorization", bearer(arbitro))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partita.stato").value("REFERTATA"))
                .andExpect(jsonPath("$.partita.setCasa").value(3))
                .andExpect(jsonPath("$.partita.setOspite").value(1))
                .andExpect(jsonPath("$.hashReferto").isNotEmpty());

        assertThat(setRepo.findByPartitaIdOrderByNumeroSet(partita.getId())).hasSize(4);
        assertThat(statistiche.findByPartitaId(partita.getId())).hasSize(2);
    }

    @Test
    void erroreSuUnaStatisticaAnnullaTuttoIlReferto() throws Exception {
        // i set sono validi e vengono scritti per primi; la seconda statistica riguarda un'atleta non convocata
        var req = new RefertoRequest(treAUno(), List.of(prest(rosaCasa.get(0), 18), prest(nonConvocata, 5)), null, "password");

        mvc.perform(post("/api/partite/{id}/referto", partita.getId()).header("Authorization", bearer(arbitro))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messaggio").value(org.hamcrest.Matchers.containsString("non e' presente")));

        // ROLLBACK: nessun set, nessuna statistica, stato invariato
        assertThat(setRepo.findByPartitaIdOrderByNumeroSet(partita.getId())).isEmpty();
        assertThat(statistiche.findByPartitaId(partita.getId())).isEmpty();
        Partita dopo = partite.findById(partita.getId()).orElseThrow();
        assertThat(dopo.getStato()).isEqualTo(StatoPartita.IN_CORSO);
        assertThat(dopo.getSetCasa()).isNull();
    }

    @Test
    void setIrregolareRestituisceTuttiGliErrori() throws Exception {
        var set = List.of(new ParzialeSet(1, 25, 24, 0, 0), new ParzialeSet(2, 25, 20, 0, 0), new ParzialeSet(3, 25, 20, 0, 0));
        var req = new RefertoRequest(set, List.of(), null, "password");

        mvc.perform(post("/api/partite/{id}/referto", partita.getId()).header("Authorization", bearer(arbitro))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errori[0]").value(org.hamcrest.Matchers.containsString("Set 1 (25-24)")));
        assertThat(partite.findById(partita.getId()).orElseThrow().getStato()).isEqualTo(StatoPartita.IN_CORSO);
    }

    @Test
    void firmaConPasswordErrataRifiutata() throws Exception {
        var req = new RefertoRequest(treAUno(), List.of(), null, "sbagliata");
        mvc.perform(post("/api/partite/{id}/referto", partita.getId()).header("Authorization", bearer(arbitro))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.messaggio").value(org.hamcrest.Matchers.containsString("Firma non valida")));
    }

    @Test
    void omologazioneAggiornaLaClassifica() throws Exception {
        var req = new RefertoRequest(treAUno(), List.of(prest(rosaCasa.get(0), 18)), null, "password");
        mvc.perform(post("/api/partite/{id}/referto", partita.getId()).header("Authorization", bearer(arbitro))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req))).andExpect(status().isOk());

        // prima dell'omologazione il risultato non conta
        mvc.perform(get("/api/campionati/{id}/classifica", partita.getCampionato().getId()).header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$[0].punti").value(0));

        mvc.perform(post("/api/partite/{id}/omologa", partita.getId()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stato").value("OMOLOGATA"));

        mvc.perform(get("/api/campionati/{id}/classifica", partita.getCampionato().getId()).header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$[0].squadra").value("Estense Ferrara"))
                .andExpect(jsonPath("$[0].punti").value(3))
                .andExpect(jsonPath("$[0].setVinti").value(3))
                .andExpect(jsonPath("$[0].puntiFatti").value(99))
                .andExpect(jsonPath("$[1].punti").value(0));
    }

    @Test
    void arbitroNonDesignatoNonPuoRefertare() throws Exception {
        Utente altro = utenti.save(Utente.builder().email("altro@test.it").password(encoder.encode("password"))
                .nome("Altro").cognome("Arbitro").ruolo(Ruolo.ARBITRO).provincia("PC").build());
        var req = new RefertoRequest(treAUno(), List.of(), null, "password");
        mvc.perform(post("/api/partite/{id}/referto", partita.getId()).header("Authorization", bearer(altro))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
