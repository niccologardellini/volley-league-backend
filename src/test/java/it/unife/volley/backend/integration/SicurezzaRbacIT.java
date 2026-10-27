package it.unife.volley.backend.integration;

import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Utente;
import it.unife.volley.backend.repository.UtenteRepository;
import it.unife.volley.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Autenticazione JWT e autorizzazione per ruolo sugli endpoint REST. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SicurezzaRbacIT {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @Autowired UtenteRepository utenti;
    @Autowired PasswordEncoder encoder;

    private Utente utente(String email, Ruolo ruolo) {
        return utenti.save(Utente.builder().email(email).password(encoder.encode("password"))
                .nome("Nome").cognome("Cognome").ruolo(ruolo).build());
    }

    private static final String CAMPIONATO = """
            {"nome":"Serie D","categoria":"Serie D Maschile","stagione":"2026/2027","girone":"B"}""";

    @Test
    void loginRestituisceTokenEProfilo() throws Exception {
        utente("login@test.it", Ruolo.ARBITRO);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@test.it\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.utente.ruolo").value("ARBITRO"));
    }

    @Test
    void credenzialiErrate401() throws Exception {
        utente("login2@test.it", Ruolo.ARBITRO);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login2@test.it\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void senzaToken401() throws Exception {
        mvc.perform(get("/api/campionati")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenManomesso401() throws Exception {
        String token = jwt.genera(utente("x@test.it", Ruolo.ADMIN));
        String[] parti = token.split("\\.");
        String firmaAlterata = (parti[2].charAt(0) == 'A' ? 'B' : 'A') + parti[2].substring(1);
        mvc.perform(get("/api/campionati").header("Authorization", "Bearer " + parti[0] + "." + parti[1] + "." + firmaAlterata))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void giocatoreNonPuoCreareCampionati403() throws Exception {
        String token = jwt.genera(utente("g@test.it", Ruolo.GIOCATORE));
        mvc.perform(post("/api/campionati").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(CAMPIONATO))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreaCampionato201() throws Exception {
        String token = jwt.genera(utente("a@test.it", Ruolo.ADMIN));
        mvc.perform(post("/api/campionati").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(CAMPIONATO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stato").value("ISCRIZIONI_APERTE"));
    }

    @Test
    void validazioneInputCampionato400() throws Exception {
        String token = jwt.genera(utente("a2@test.it", Ruolo.ADMIN));
        mvc.perform(post("/api/campionati").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"categoria\":\"C\",\"stagione\":\"2026\",\"girone\":\"A\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errori.length()").value(2));
    }

    @Test
    void arbitroNonAccedeAllAreaDirigente403() throws Exception {
        String token = jwt.genera(utente("arb@test.it", Ruolo.ARBITRO));
        mvc.perform(get("/api/squadre/mie").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
