package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.calendario.Accoppiamento;
import it.unife.volley.backend.domain.calendario.GeneratoreCalendario;
import it.unife.volley.backend.dto.*;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.exception.NotFoundException;
import it.unife.volley.backend.model.*;
import it.unife.volley.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Creazione campionati, iscrizione squadre e generazione automatica del calendario. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampionatoService {

    /** Numero minimo di tesserati per poter iscrivere una squadra. */
    public static final int MIN_TESSERATI_ISCRIZIONE = 6;

    private final CampionatoRepository campionatoRepository;
    private final SquadraRepository squadraRepository;
    private final PartitaRepository partitaRepository;
    private final TesseratoRepository tesseratoRepository;
    private final SquadraService squadraService;
    private final GeneratoreCalendario generatoreCalendario;
    private final DtoMapper mapper;

    public List<CampionatoDto> elenco() {
        return campionatoRepository.findAllByOrderByStagioneDescNomeAscGironeAsc().stream().map(this::dto).toList();
    }

    public CampionatoDto dettaglio(Long id) {
        return dto(carica(id));
    }

    @Transactional
    public CampionatoDto crea(CampionatoRequest req) {
        Campionato c = Campionato.builder()
                .nome(req.nome().trim()).categoria(req.categoria().trim())
                .stagione(req.stagione()).girone(req.girone().trim().toUpperCase())
                .stato(StatoCampionato.ISCRIZIONI_APERTE).build();
        return dto(campionatoRepository.save(c));
    }

    public List<SquadraDto> squadre(Long idCampionato) {
        carica(idCampionato);
        return squadraRepository.findByCampionatoId(idCampionato).stream()
                .map(s -> mapper.squadra(s, tesseratoRepository.findBySquadraIdOrderByNumeroMaglia(s.getId()).size()))
                .toList();
    }

    /** Iscrizione di una squadra (del dirigente autenticato) a un campionato con iscrizioni aperte. */
    @Transactional
    public SquadraDto iscrivi(Long idCampionato, Long idSquadra) {
        Campionato c = carica(idCampionato);
        Squadra s = squadraService.caricaConProprieta(idSquadra);
        if (c.getStato() != StatoCampionato.ISCRIZIONI_APERTE) {
            throw new BusinessException("Le iscrizioni a " + c.getNome() + " sono chiuse");
        }
        if (s.getCampionato() != null) {
            throw new BusinessException(s.getNome() + " e' gia' iscritta a " + s.getCampionato().getNome());
        }
        int tesserati = tesseratoRepository.findBySquadraIdOrderByNumeroMaglia(idSquadra).size();
        if (tesserati < MIN_TESSERATI_ISCRIZIONE) {
            throw new BusinessException("Servono almeno " + MIN_TESSERATI_ISCRIZIONE + " tesserati per iscriversi (attuali: " + tesserati + ")");
        }
        s.setCampionato(c);
        return mapper.squadra(s, tesserati);
    }

    /**
     * Genera il calendario con l'algoritmo di Berger e chiude le iscrizioni.
     * Operazione atomica: o vengono create tutte le partite o nessuna.
     */
    @Transactional
    public List<PartitaDto> generaCalendario(Long idCampionato, GenerazioneCalendarioRequest req) {
        Campionato c = carica(idCampionato);
        if (c.getStato() != StatoCampionato.ISCRIZIONI_APERTE || partitaRepository.countByCampionatoId(idCampionato) > 0) {
            throw new BusinessException("Il calendario di questo campionato e' gia' stato generato");
        }
        List<Squadra> squadre = squadraRepository.findByCampionatoId(idCampionato);
        if (squadre.size() < 2) {
            throw new BusinessException("Servono almeno 2 squadre iscritte per generare il calendario");
        }
        List<Accoppiamento<Squadra>> turni = generatoreCalendario.genera(
                squadre, req.dataInizio(), req.ora(), req.giorniTraGiornate(), req.andataRitorno());
        List<Partita> partite = turni.stream().map(a -> Partita.builder()
                .campionato(c).giornata(a.giornata())
                .squadraCasa(a.casa()).squadraOspite(a.ospite())
                .dataOra(a.dataOra()).stato(StatoPartita.PROGRAMMATA).build()).toList();
        partitaRepository.saveAll(partite);
        c.setStato(StatoCampionato.IN_CORSO);
        return partite.stream().map(mapper::partita).toList();
    }

    Campionato carica(Long id) {
        return campionatoRepository.findById(id).orElseThrow(() -> new NotFoundException("Campionato", id));
    }

    private CampionatoDto dto(Campionato c) {
        return new CampionatoDto(c.getId(), c.getNome(), c.getCategoria(), c.getStagione(), c.getGirone(), c.getStato(),
                squadraRepository.findByCampionatoId(c.getId()).size(), partitaRepository.countByCampionatoId(c.getId()));
    }
}
