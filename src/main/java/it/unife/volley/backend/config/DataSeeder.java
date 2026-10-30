package it.unife.volley.backend.config;

import it.unife.volley.backend.domain.calendario.Accoppiamento;
import it.unife.volley.backend.domain.calendario.GeneratoreCalendario;
import it.unife.volley.backend.domain.designazione.VincoliDesignazione;
import it.unife.volley.backend.domain.referto.ParzialeSet;
import it.unife.volley.backend.domain.referto.PrestazioneAtleta;
import it.unife.volley.backend.domain.referto.RefertoFactory;
import it.unife.volley.backend.domain.referto.RefertoValidator;
import it.unife.volley.backend.model.*;
import it.unife.volley.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Popola il database con un campionato dimostrativo gia' avviato, cosi' che ogni ruolo
 * trovi subito dati significativi. Viene eseguito solo se la tabella utente e' vuota.
 * Tutti gli account hanno password {@code password}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final String PASSWORD = "password";
    /** Ultima giornata completamente omologata nel dataset dimostrativo. */
    private static final int GIORNATE_OMOLOGATE = 3;

    private final UtenteRepository utenteRepository;
    private final SocietaRepository societaRepository;
    private final CampionatoRepository campionatoRepository;
    private final SquadraRepository squadraRepository;
    private final TesseratoRepository tesseratoRepository;
    private final PartitaRepository partitaRepository;
    private final ListaGaraRepository listaGaraRepository;
    private final StatisticaGiocatoreRepository statisticaRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeneratoreCalendario generatoreCalendario;
    private final VincoliDesignazione vincoli;
    private final RefertoValidator refertoValidator;
    private final RefertoFactory refertoFactory;

    private final Random random = new Random(2026);
    /** Generatore separato per i nomi, cosi' i risultati delle partite non dipendono dagli elenchi di nomi. */
    private final Random randomNomi = new Random(7);
    private final Deque<String> cognomiLiberi = new ArrayDeque<>();
    private final Map<Long, Deque<String>> nomiLiberiPerSquadra = new HashMap<>();
    private final Map<Long, String[]> staffPerSquadra = new HashMap<>();
    private String passwordHash;

    @Override
    @Transactional
    public void run(String... args) {
        if (utenteRepository.count() > 0) {
            return;
        }
        log.info("Database vuoto: caricamento dati dimostrativi");
        passwordHash = passwordEncoder.encode(PASSWORD);
        preparaCognomi();

        utente("admin@volley.it", "Giuseppe", "Manfredi", Ruolo.ADMIN, null, "BO");

        Campionato serieD = campionatoRepository.save(Campionato.builder()
                .nome("Serie D Maschile").categoria("Serie D Maschile").stagione("2026/2027")
                .girone("A").stato(StatoCampionato.IN_CORSO).build());
        Campionato serieC = campionatoRepository.save(Campionato.builder()
                .nome("Serie C Maschile").categoria("Serie C Maschile").stagione("2026/2027")
                .girone("A").stato(StatoCampionato.ISCRIZIONI_APERTE).build());

        // Societa' emiliano-romagnole: una squadra ciascuna e un dirigente
        Map<Long, List<Tesserato>> rose = new HashMap<>();
        List<Squadra> squadreD = new ArrayList<>();
        for (String[] s : SERIE_D) {
            squadreD.add(squadraConRosa(s, serieD, rose));
        }
        Squadra quattroTorri = squadreD.get(0);
        List<Squadra> squadreC = new ArrayList<>();
        for (String[] s : SERIE_C) {
            squadreC.add(squadraConRosa(s, serieC, rose));
        }

        // 4 Torri ha 16 tesserati (3 liberi) per rendere significativa la scelta della lista gara
        List<Tesserato> rosaQuattroTorri = rose.get(quattroTorri.getId());
        rosaQuattroTorri.add(tesserato(quattroTorri, RuoloCampo.LIBERO, 15));
        rosaQuattroTorri.add(tesserato(quattroTorri, RuoloCampo.SCHIACCIATORE, 16));
        Tesserato gardellini = rosaQuattroTorri.stream().filter(t -> t.getNumeroMaglia() == 7).findFirst().orElseThrow();
        gardellini.setNome("Niccolò");
        gardellini.setCognome("Gardellini");
        gardellini.setUtente(utente("giocatore@4torri.it", gardellini.getNome(), gardellini.getCognome(),
                Ruolo.GIOCATORE, null, "FE"));

        // Arbitri con affiliazioni/province diverse per mostrare i vincoli di designazione
        List<Utente> creati = new ArrayList<>();
        for (String[] a : ARBITRI) {
            Societa affiliazione = a[3].equals("MO") ? squadreC.get(0).getSocieta() : null;
            creati.add(utente(a[0], a[1], a[2], Ruolo.ARBITRO, affiliazione, a[4]));
        }
        Utente rossi = creati.get(0);
        List<Utente> arbitri = utenteRepository.findByRuolo(Ruolo.ARBITRO);

        // Calendario Serie D: andata e ritorno dal 26/09/2026, sabato ore 20:30.
        // La Serie C ha le 8 squadre iscritte e attende la generazione del calendario dal giudice.
        List<Accoppiamento<Squadra>> turni = generatoreCalendario.genera(
                squadreD, LocalDate.of(2026, 9, 26), LocalTime.of(20, 30), 7, true);
        List<Partita> partite = new ArrayList<>();
        for (Accoppiamento<Squadra> a : turni) {
            partite.add(partitaRepository.save(Partita.builder().campionato(serieD).giornata(a.giornata())
                    .squadraCasa(a.casa()).squadraOspite(a.ospite()).dataOra(a.dataOra())
                    .stato(StatoPartita.PROGRAMMATA).build()));
        }

        for (Partita p : partite) {
            int g = p.getGiornata();
            if (g <= GIORNATE_OMOLOGATE + 1) {
                designa(p, arbitri);
                inviaListe(p, rose);
                boolean refertata = g == GIORNATE_OMOLOGATE + 1 && coinvolge(p, quattroTorri);
                chiudi(p, rose, refertata ? StatoPartita.REFERTATA : StatoPartita.OMOLOGATA);
            } else if (g == GIORNATE_OMOLOGATE + 2) {
                if (coinvolge(p, quattroTorri)) {
                    // 4 Torri deve ancora inviare la propria lista: demo del dirigente
                    designa(p, arbitri.stream().filter(a -> a != rossi).toList());
                    Squadra avversaria = p.getSquadraCasa() == quattroTorri ? p.getSquadraOspite() : p.getSquadraCasa();
                    lista(p, avversaria, rose.get(avversaria.getId()));
                } else if (p.getArbitro() == null && compatibile(rossi, p) && partite.stream()
                        .noneMatch(x -> x.getGiornata() == g && x.getArbitro() == rossi)) {
                    // Gara pronta per l'arbitro Rossi: liste inviate, puo' avviarla e refertarla
                    p.setArbitro(rossi);
                    inviaListe(p, rose);
                    p.setStato(StatoPartita.ROSTER_INVIATI);
                }
            }
        }
        log.info("Dati dimostrativi caricati: {} partite, {} arbitri", partite.size(), arbitri.size());
    }

    private Utente utente(String email, String nome, String cognome, Ruolo ruolo, Societa societa, String provincia) {
        return utenteRepository.save(Utente.builder().email(email).password(passwordHash).nome(nome).cognome(cognome)
                .ruolo(ruolo).societa(societa).provincia(provincia).build());
    }

    private Societa societa(String nome, String codice, String citta, String provincia) {
        return societaRepository.save(Societa.builder().nome(nome).codiceFipav(codice).citta(citta).provincia(provincia).build());
    }

    private Squadra squadra(String nome, Societa societa, Campionato campionato) {
        return squadraRepository.save(Squadra.builder().nome(nome).societa(societa).campionato(campionato).build());
    }

    /** Colonne: societa', squadra, citta', provincia, dominio email del dirigente. */
    private static final String[][] SERIE_D = {
            {"Pallavolo 4 Torri", "4 Torri Ferrara", "Ferrara", "FE", "4torri.it"},
            {"Zinella Volley", "Zinella Volley", "San Lazzaro di Savena", "BO", "zinellavolley.it"},
            {"Pallavolo Bologna", "Pallavolo Bologna", "Bologna", "BO", "pallavolobologna.it"},
            {"Faenza Volley", "Faenza Volley", "Faenza", "RA", "faenzavolley.it"},
            {"Pallavolo Molinella", "Pallavolo Molinella", "Molinella", "BO", "pallavolomolinella.it"},
            {"Volley Mirandola", "Volley Mirandola", "Mirandola", "MO", "volleymirandola.it"},
            {"Porto Robur Costa", "Robur Costa Ravenna", "Ravenna", "RA", "roburcosta.it"},
            {"Pallavolo Massa Lombarda", "Massa Lombarda Volley", "Massa Lombarda", "RA", "pallavolomassalombarda.it"},
            {"Pallavolo Correggio", "Pallavolo Correggio", "Correggio", "RE", "pallavolocorreggio.it"},
            {"Pallavolo Castenaso", "Pallavolo Castenaso", "Castenaso", "BO", "pallavolocastenaso.it"}};
    private static final String[][] SERIE_C = {
            {"Anderlini Modena", "Anderlini Modena", "Modena", "MO", "anderlini.it"},
            {"Modena Volley", "Modena Volley", "Modena", "MO", "modenavolley.it"},
            {"Benedetto Volley", "Benedetto Volley Cesena", "Cesena", "FC", "benedettovolley.it"},
            {"Rimini Volley", "Rimini Volley", "Rimini", "RN", "riminivolley.it"},
            {"Volley Forlì", "Volley Forlì", "Forlì", "FC", "volleyforli.it"},
            {"Volley Tricolore", "Volley Tricolore Reggio Emilia", "Reggio Emilia", "RE", "volleytricolore.it"},
            {"Pallavolo Parma", "Pallavolo Parma", "Parma", "PR", "pallavoloparma.it"},
            {"Imola Volley", "Imola Volley", "Imola", "BO", "imolavolley.it"}};
    private static final String[] DIRIGENTI = {"Alberto Bova", "Elena Gallo", "Paolo Conti", "Davide Ricci",
            "Anna Marchetti", "Roberto Zanni", "Silvia Montanari", "Giorgio Bertoni", "Laura Benassi", "Fabio Guidi",
            "Carlo Malaguti", "Monica Pancaldi", "Enrico Golinelli", "Valeria Tosi", "Massimo Casadei",
            "Paola Ferrari", "Stefano Cavazza", "Claudia Rambaldi"};

    private int societaCreate = 0;

    private Squadra squadraConRosa(String[] dati, Campionato campionato, Map<Long, List<Tesserato>> rose) {
        int n = societaCreate++;
        Societa societa = societa(dati[0], String.format("%s-%04d", dati[3], 101 + n * 37), dati[2], dati[3]);
        String[] dirigente = DIRIGENTI[n].split(" ");
        utente("dirigente@" + dati[4], dirigente[0], dirigente[1], Ruolo.DIRIGENTE, societa, dati[3]);
        Squadra squadra = squadra(dati[1], societa, campionato);
        List<String> nomi = new ArrayList<>(List.of(NomiDemo.NOMI));
        Collections.shuffle(nomi, randomNomi);
        nomiLiberiPerSquadra.put(squadra.getId(), new ArrayDeque<>(nomi));
        staffPerSquadra.put(squadra.getId(), new String[]{persona(squadra), persona(squadra)});
        rose.put(squadra.getId(), rosa(squadra, 14));
        return squadra;
    }

    /** Arbitri: email, nome, cognome, sigla dell'affiliazione (MO = societa' modenese), provincia. */
    private static final String[][] ARBITRI = {
            {"arbitro@fipav.it", "Luca", "Rossi", "", "RE"},
            {"arbitro.lodi@fipav.it", "Chiara", "Lodi", "", "PC"},
            {"arbitro.verdi@fipav.it", "Matteo", "Verdi", "", "FC"},
            {"arbitro.mazza@fipav.it", "Federica", "Mazza", "", "FE"},
            {"arbitro.serra@fipav.it", "Stefano", "Serra", "MO", "MO"},
            {"arbitro.bassi@fipav.it", "Elisa", "Bassi", "", "PR"},
            {"arbitro.gori@fipav.it", "Andrea", "Gori", "", "RN"},
            {"arbitro.fabbri@fipav.it", "Sara", "Fabbri", "", "BO"}};

    /** Mescola i cognomi escludendo quelli gia' usati da giudice, dirigenti, arbitri e dal giocatore demo. */
    private void preparaCognomi() {
        Set<String> riservati = new HashSet<>(List.of("Manfredi", "Gardellini"));
        for (String d : DIRIGENTI) riservati.add(d.split(" ")[1]);
        for (String[] a : ARBITRI) riservati.add(a[2]);
        List<String> cognomi = new ArrayList<>(List.of(NomiDemo.COGNOMI));
        cognomi.removeAll(riservati);
        Collections.shuffle(cognomi, randomNomi);
        cognomiLiberi.addAll(cognomi);
    }

    /** Nome non ancora usato nella squadra e cognome non ancora usato nel database. */
    private String[] nuovoNomeCognome(Squadra s) {
        return new String[]{nomiLiberiPerSquadra.get(s.getId()).pop(), cognomiLiberi.pop()};
    }

    private String persona(Squadra s) {
        return String.join(" ", nuovoNomeCognome(s));
    }

    /** Composizione tipo di una rosa (le prime 14 posizioni includono esattamente 2 liberi). */
    private static final RuoloCampo[] RUOLI = {
            RuoloCampo.PALLEGGIATORE, RuoloCampo.SCHIACCIATORE, RuoloCampo.CENTRALE, RuoloCampo.OPPOSTO,
            RuoloCampo.SCHIACCIATORE, RuoloCampo.CENTRALE, RuoloCampo.LIBERO, RuoloCampo.PALLEGGIATORE,
            RuoloCampo.SCHIACCIATORE, RuoloCampo.CENTRALE, RuoloCampo.OPPOSTO, RuoloCampo.SCHIACCIATORE,
            RuoloCampo.LIBERO, RuoloCampo.CENTRALE, RuoloCampo.LIBERO, RuoloCampo.SCHIACCIATORE};

    private List<Tesserato> rosa(Squadra s, int numero) {
        List<Tesserato> rosa = new ArrayList<>();
        int[] maglie = {7, 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < numero; i++) {
            RuoloCampo ruolo = RUOLI[i];
            if (i == 0) ruolo = RuoloCampo.SCHIACCIATORE;
            if (i == 1) ruolo = RuoloCampo.PALLEGGIATORE;
            rosa.add(tesserato(s, ruolo, maglie[i]));
        }
        return rosa;
    }

    private Tesserato tesserato(Squadra s, RuoloCampo ruolo, int maglia) {
        String[] nomeCognome = nuovoNomeCognome(s);
        return tesseratoRepository.save(Tesserato.builder()
                .nome(nomeCognome[0]).cognome(nomeCognome[1])
                .ruoloCampo(ruolo).numeroMaglia(maglia).squadra(s).build());
    }

    private boolean coinvolge(Partita p, Squadra s) {
        return p.getSquadraCasa() == s || p.getSquadraOspite() == s;
    }

    private boolean compatibile(Utente arbitro, Partita p) {
        return vincoli.violazioni(arbitro, p.getSquadraCasa(), p.getSquadraOspite(), false).isEmpty();
    }

    /** Assegna il primo arbitro compatibile e non gia' impegnato nella stessa giornata. */
    private void designa(Partita p, List<Utente> arbitri) {
        List<Utente> candidati = new ArrayList<>(arbitri);
        Collections.rotate(candidati, p.getGiornata());
        for (Utente a : candidati) {
            boolean impegnato = !partitaRepository.findImpegniArbitro(a.getId(), p.getId(),
                    p.getDataOra().toLocalDate().atStartOfDay(), p.getDataOra().toLocalDate().atTime(23, 59)).isEmpty();
            if (vincoli.violazioni(a, p.getSquadraCasa(), p.getSquadraOspite(), impegnato).isEmpty()) {
                p.setArbitro(a);
                partitaRepository.saveAndFlush(p);
                return;
            }
        }
    }

    private void inviaListe(Partita p, Map<Long, List<Tesserato>> rose) {
        lista(p, p.getSquadraCasa(), rose.get(p.getSquadraCasa().getId()));
        lista(p, p.getSquadraOspite(), rose.get(p.getSquadraOspite().getId()));
    }

    private void lista(Partita p, Squadra s, List<Tesserato> rosa) {
        listaGaraRepository.save(ListaGara.builder().partita(p).squadra(s)
                .atleti(new LinkedHashSet<>(rosa.subList(0, 14)))
                .allenatore(staffPerSquadra.get(s.getId())[0])
                .dirigenteAccompagnatore(staffPerSquadra.get(s.getId())[1])
                .inviataAt(p.getDataOra().minusDays(1)).build());
    }

    /** Simula una gara giocata: parziali regolari, statistiche e firma del referto. */
    private void chiudi(Partita p, Map<Long, List<Tesserato>> rose, StatoPartita statoFinale) {
        List<ParzialeSet> set = generaParziali();
        var esito = refertoValidator.valida(set);
        set.forEach(s -> p.getSet().add(refertoFactory.creaSet(p, s)));

        List<PrestazioneAtleta> prestazioni = new ArrayList<>();
        prestazioni.addAll(prestazioni(rose.get(p.getSquadraCasa().getId()), esito.puntiCasa()));
        prestazioni.addAll(prestazioni(rose.get(p.getSquadraOspite().getId()), esito.puntiOspite()));
        Map<Long, Tesserato> perId = new HashMap<>();
        rose.values().forEach(r -> r.forEach(t -> perId.put(t.getId(), t)));
        for (PrestazioneAtleta pa : prestazioni) {
            statisticaRepository.save(refertoFactory.creaStatistica(p, perId.get(pa.idTesserato()), pa));
        }

        p.setSetCasa(esito.setCasa());
        p.setSetOspite(esito.setOspite());
        p.setFirmaArbitroAt(p.getDataOra().plusHours(2));
        p.setHashReferto(refertoFactory.impronta(p, set, prestazioni, p.getArbitro().getEmail()));
        p.setNoteReferto(random.nextInt(4) == 0 ? "Gara regolare, nessun incidente." : null);
        p.setStato(statoFinale);
        if (statoFinale == StatoPartita.OMOLOGATA) {
            p.setOmologataAt(p.getDataOra().plusDays(3));
        }
        partitaRepository.save(p);
    }

    private List<ParzialeSet> generaParziali() {
        int setPerdente = random.nextInt(3); // 0, 1 o 2 set al perdente
        boolean vinceCasa = random.nextBoolean();
        List<Boolean> esiti = new ArrayList<>();
        for (int i = 0; i < setPerdente; i++) esiti.add(!vinceCasa);
        for (int i = 0; i < 2; i++) esiti.add(vinceCasa);
        Collections.shuffle(esiti, random);
        esiti.add(vinceCasa); // l'ultimo set lo vince sempre la vincente
        List<ParzialeSet> set = new ArrayList<>();
        for (int i = 0; i < esiti.size(); i++) {
            int numero = i + 1;
            boolean tieBreak = numero == 5;
            int soglia = tieBreak ? 15 : 25;
            int vincente, perdente;
            if (random.nextInt(6) == 0) {
                vincente = soglia + 1 + random.nextInt(3);
                perdente = vincente - 2;
            } else {
                vincente = soglia;
                perdente = tieBreak ? 6 + random.nextInt(8) : 14 + random.nextInt(10);
            }
            boolean casa = esiti.get(i);
            set.add(new ParzialeSet(numero, casa ? vincente : perdente, casa ? perdente : vincente,
                    random.nextInt(7), random.nextInt(7)));
        }
        return set;
    }

    /** Distribuisce circa il 60% dei punti di squadra (il resto sono errori avversari) tra 8 atleti. */
    private List<PrestazioneAtleta> prestazioni(List<Tesserato> rosa, int puntiSquadra) {
        List<Tesserato> inCampo = rosa.subList(0, 8);
        int[] pesi = new int[inCampo.size()];
        int totalePesi = 0;
        for (int i = 0; i < inCampo.size(); i++) {
            pesi[i] = switch (inCampo.get(i).getRuoloCampo()) {
                case SCHIACCIATORE, OPPOSTO -> 5;
                case CENTRALE -> 3;
                case PALLEGGIATORE -> 1;
                case LIBERO -> 0;
            };
            totalePesi += pesi[i];
        }
        int daDistribuire = (int) (puntiSquadra * 0.6);
        List<PrestazioneAtleta> out = new ArrayList<>();
        for (int i = 0; i < inCampo.size(); i++) {
            Tesserato t = inCampo.get(i);
            int punti = totalePesi == 0 ? 0 : daDistribuire * pesi[i] / totalePesi + (pesi[i] > 0 ? random.nextInt(3) : 0);
            int ace = punti == 0 ? 0 : random.nextInt(Math.min(3, punti) + 1);
            int muri = t.getRuoloCampo() == RuoloCampo.CENTRALE ? Math.min(punti - ace, 1 + random.nextInt(4))
                    : Math.min(punti - ace, random.nextInt(2));
            Sanzione cartellino = random.nextInt(25) == 0 ? Sanzione.GIALLO : Sanzione.NESSUNA;
            out.add(new PrestazioneAtleta(t.getId(), punti, ace, muri, random.nextInt(6), cartellino));
        }
        return out;
    }
}
