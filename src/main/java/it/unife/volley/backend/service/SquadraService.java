package it.unife.volley.backend.service;

import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.SquadraDto;
import it.unife.volley.backend.dto.SquadraRequest;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.exception.ForbiddenException;
import it.unife.volley.backend.exception.NotFoundException;
import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Squadra;
import it.unife.volley.backend.model.Utente;
import it.unife.volley.backend.repository.SquadraRepository;
import it.unife.volley.backend.repository.TesseratoRepository;
import it.unife.volley.backend.security.UtenteCorrente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/** Squadre della societa' del dirigente autenticato e controlli di proprieta'. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SquadraService {

    private final SquadraRepository squadraRepository;
    private final TesseratoRepository tesseratoRepository;
    private final UtenteCorrente utenteCorrente;
    private final DtoMapper mapper;

    public List<SquadraDto> mieSquadre() {
        Utente dirigente = utenteCorrente.entita();
        if (dirigente.getSocieta() == null) {
            throw new BusinessException("Il tuo profilo non e' associato ad alcuna societa'");
        }
        return squadraRepository.findBySocietaId(dirigente.getSocieta().getId()).stream()
                .map(s -> mapper.squadra(s, tesseratoRepository.findBySquadraIdOrderByNumeroMaglia(s.getId()).size()))
                .toList();
    }

    @Transactional
    public SquadraDto crea(SquadraRequest req) {
        Utente dirigente = utenteCorrente.entita();
        if (dirigente.getSocieta() == null) {
            throw new BusinessException("Il tuo profilo non e' associato ad alcuna societa'");
        }
        Squadra s = squadraRepository.save(Squadra.builder().nome(req.nome().trim()).societa(dirigente.getSocieta()).build());
        return mapper.squadra(s, 0);
    }

    /**
     * Carica la squadra verificando che appartenga alla societa' del dirigente autenticato.
     * L'admin ha visibilita' su tutte le squadre.
     */
    public Squadra caricaConProprieta(Long idSquadra) {
        Squadra s = squadraRepository.findById(idSquadra).orElseThrow(() -> new NotFoundException("Squadra", idSquadra));
        Utente u = utenteCorrente.entita();
        if (u.getRuolo() == Ruolo.ADMIN) return s;
        if (u.getSocieta() == null || !Objects.equals(u.getSocieta().getId(), s.getSocieta().getId())) {
            throw new ForbiddenException("La squadra non appartiene alla tua societa'");
        }
        return s;
    }
}
