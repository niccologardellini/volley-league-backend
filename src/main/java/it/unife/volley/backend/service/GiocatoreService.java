package it.unife.volley.backend.service;

import it.unife.volley.backend.dto.AreaGiocatoreDto;
import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.PresenzaDto;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.model.*;
import it.unife.volley.backend.repository.ListaGaraRepository;
import it.unife.volley.backend.repository.PartitaRepository;
import it.unife.volley.backend.repository.StatisticaGiocatoreRepository;
import it.unife.volley.backend.repository.TesseratoRepository;
import it.unife.volley.backend.security.UtenteCorrente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Area personale del giocatore: totali stagionali, storico convocazioni e calendario. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiocatoreService {

    private final TesseratoRepository tesseratoRepository;
    private final StatisticaGiocatoreRepository statisticaRepository;
    private final ListaGaraRepository listaGaraRepository;
    private final PartitaRepository partitaRepository;
    private final UtenteCorrente utenteCorrente;
    private final DtoMapper mapper;

    public AreaGiocatoreDto areaPersonale() {
        Tesserato t = tesseratoRepository.findByUtenteId(utenteCorrente.principal().id())
                .orElseThrow(() -> new BusinessException("Il tuo account non e' collegato ad alcun tesseramento"));
        Squadra squadra = t.getSquadra();

        Object[] tot = statisticaRepository.totaliStagionali(t.getId(), StatoPartita.OMOLOGATA).get(0);
        long presenze = ((Number) tot[0]).longValue();
        long punti = ((Number) tot[1]).longValue();
        List<ListaGara> convocazioni = listaGaraRepository.findByAtleta(t.getId());
        var totali = new AreaGiocatoreDto.Totali(convocazioni.size(), presenze, punti,
                ((Number) tot[2]).longValue(), ((Number) tot[3]).longValue(), ((Number) tot[4]).longValue(),
                presenze == 0 ? 0 : Math.round(punti * 10.0 / presenze) / 10.0);

        Map<Long, StatisticaGiocatore> statPerPartita = statisticaRepository.findByTesseratoAndStato(t.getId(), StatoPartita.OMOLOGATA)
                .stream().collect(Collectors.toMap(s -> s.getPartita().getId(), Function.identity()));
        List<PresenzaDto> storico = convocazioni.stream().map(l -> {
            Partita p = l.getPartita();
            boolean inCasa = Objects.equals(p.getSquadraCasa().getId(), squadra.getId());
            Squadra avversario = inCasa ? p.getSquadraOspite() : p.getSquadraCasa();
            String risultato = p.getSetCasa() == null ? null
                    : inCasa ? p.getSetCasa() + "-" + p.getSetOspite() : p.getSetOspite() + "-" + p.getSetCasa();
            StatisticaGiocatore s = statPerPartita.get(p.getId());
            return new PresenzaDto(p.getId(), p.getDataOra(), avversario.getNome(), inCasa, p.getStato(), risultato,
                    s == null ? null : s.getPuntiFatti(), s == null ? null : s.getAce(), s == null ? null : s.getMuri(),
                    s == null ? null : s.getErrori(), s == null ? null : s.getCartellino());
        }).toList();

        Campionato c = squadra.getCampionato();
        return new AreaGiocatoreDto(mapper.tesserato(t), mapper.ref(squadra),
                c == null ? null : c.getNome() + " - Girone " + c.getGirone(), totali, storico,
                partitaRepository.findBySquadra(squadra.getId()).stream().map(mapper::partita).toList());
    }
}
