package it.unife.volley.backend.service;

import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.TesseratoDto;
import it.unife.volley.backend.dto.TesseratoRequest;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.exception.NotFoundException;
import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Squadra;
import it.unife.volley.backend.model.Tesserato;
import it.unife.volley.backend.model.Utente;
import it.unife.volley.backend.repository.ListaGaraRepository;
import it.unife.volley.backend.repository.TesseratoRepository;
import it.unife.volley.backend.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Tesseramento degli atleti e gestione della rosa ufficiale. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TesseratoService {

    /** Password iniziale degli account giocatore creati dal dirigente. */
    public static final String PASSWORD_INIZIALE = "password";

    private final TesseratoRepository tesseratoRepository;
    private final ListaGaraRepository listaGaraRepository;
    private final UtenteRepository utenteRepository;
    private final SquadraService squadraService;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper mapper;

    public List<TesseratoDto> rosa(Long idSquadra) {
        squadraService.caricaConProprieta(idSquadra);
        return tesseratoRepository.findBySquadraIdOrderByNumeroMaglia(idSquadra).stream().map(mapper::tesserato).toList();
    }

    @Transactional
    public TesseratoDto tessera(Long idSquadra, TesseratoRequest req) {
        Squadra squadra = squadraService.caricaConProprieta(idSquadra);
        if (tesseratoRepository.existsBySquadraIdAndNumeroMaglia(idSquadra, req.numeroMaglia())) {
            throw new BusinessException("Il numero di maglia " + req.numeroMaglia() + " e' gia' assegnato");
        }
        Tesserato t = Tesserato.builder()
                .nome(req.nome().trim()).cognome(req.cognome().trim())
                .ruoloCampo(req.ruoloCampo()).numeroMaglia(req.numeroMaglia())
                .squadra(squadra)
                .utente(creaAccountGiocatore(req))
                .build();
        return mapper.tesserato(tesseratoRepository.save(t));
    }

    @Transactional
    public TesseratoDto aggiorna(Long idTesserato, TesseratoRequest req) {
        Tesserato t = carica(idTesserato);
        if (tesseratoRepository.existsBySquadraIdAndNumeroMagliaAndIdNot(t.getSquadra().getId(), req.numeroMaglia(), t.getId())) {
            throw new BusinessException("Il numero di maglia " + req.numeroMaglia() + " e' gia' assegnato");
        }
        t.setNome(req.nome().trim());
        t.setCognome(req.cognome().trim());
        t.setRuoloCampo(req.ruoloCampo());
        t.setNumeroMaglia(req.numeroMaglia());
        if (t.getUtente() == null) {
            t.setUtente(creaAccountGiocatore(req));
        }
        return mapper.tesserato(t);
    }

    /** Lo svincolo e' consentito solo se l'atleta non compare in nessuna lista gara (integrita' storica). */
    @Transactional
    public void svincola(Long idTesserato) {
        Tesserato t = carica(idTesserato);
        if (!listaGaraRepository.findByAtleta(idTesserato).isEmpty()) {
            throw new BusinessException("L'atleta e' gia' stato convocato a referto e non puo' essere eliminato");
        }
        Utente account = t.getUtente();
        tesseratoRepository.delete(t);
        if (account != null) utenteRepository.delete(account);
    }

    private Tesserato carica(Long idTesserato) {
        Tesserato t = tesseratoRepository.findById(idTesserato).orElseThrow(() -> new NotFoundException("Tesserato", idTesserato));
        squadraService.caricaConProprieta(t.getSquadra().getId());
        return t;
    }

    private Utente creaAccountGiocatore(TesseratoRequest req) {
        if (req.emailAccount() == null || req.emailAccount().isBlank()) return null;
        String email = req.emailAccount().trim().toLowerCase();
        if (utenteRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Esiste gia' un account con email " + email);
        }
        return utenteRepository.save(Utente.builder()
                .email(email).password(passwordEncoder.encode(PASSWORD_INIZIALE))
                .nome(req.nome().trim()).cognome(req.cognome().trim()).ruolo(Ruolo.GIOCATORE).build());
    }
}
