package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.stato.EventoPartita;
import it.unife.volley.backend.domain.stato.MacchinaStatiPartita;
import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.PartitaDto;
import it.unife.volley.backend.exception.ForbiddenException;
import it.unife.volley.backend.exception.NotFoundException;
import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.StatoPartita;
import it.unife.volley.backend.repository.PartitaRepository;
import it.unife.volley.backend.security.UtenteCorrente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Consultazione del calendario e avvio delle gare. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartitaService {

    private final PartitaRepository partitaRepository;
    private final MacchinaStatiPartita macchinaStati;
    private final UtenteCorrente utenteCorrente;
    private final SquadraService squadraService;
    private final DtoMapper mapper;

    public List<PartitaDto> calendario(Long idCampionato) {
        return partitaRepository.findByCampionato(idCampionato).stream().map(mapper::partita).toList();
    }

    public List<PartitaDto> perStato(List<StatoPartita> stati) {
        List<StatoPartita> filtro = stati == null || stati.isEmpty() ? Arrays.asList(StatoPartita.values()) : stati;
        return partitaRepository.findByStati(filtro).stream().map(mapper::partita).toList();
    }

    public List<PartitaDto> partiteSquadra(Long idSquadra) {
        squadraService.caricaConProprieta(idSquadra);
        return partitaRepository.findBySquadra(idSquadra).stream().map(mapper::partita).toList();
    }

    /** Designazioni dell'arbitro autenticato. */
    public List<PartitaDto> mieDesignazioni() {
        return partitaRepository.findByArbitro(utenteCorrente.principal().id()).stream().map(mapper::partita).toList();
    }

    public PartitaDto dettaglio(Long id) {
        return mapper.partita(carica(id));
    }

    /** ROSTER_INVIATI -> IN_CORSO: solo l'arbitro designato puo' avviare la gara. */
    @Transactional
    public PartitaDto avviaGara(Long id) {
        Partita p = carica(id);
        verificaArbitroDesignato(p);
        macchinaStati.applica(p, EventoPartita.AVVIA_GARA);
        return mapper.partita(p);
    }

    Partita carica(Long id) {
        return partitaRepository.findById(id).orElseThrow(() -> new NotFoundException("Partita", id));
    }

    void verificaArbitroDesignato(Partita p) {
        Long idUtente = utenteCorrente.principal().id();
        if (p.getArbitro() == null || !Objects.equals(p.getArbitro().getId(), idUtente)) {
            throw new ForbiddenException("Non sei l'arbitro designato per questa partita");
        }
    }
}
