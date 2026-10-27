package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.referto.EsitoPartita;
import it.unife.volley.backend.domain.referto.PrestazioneAtleta;
import it.unife.volley.backend.domain.referto.RefertoFactory;
import it.unife.volley.backend.domain.referto.RefertoValidator;
import it.unife.volley.backend.domain.stato.EventoPartita;
import it.unife.volley.backend.domain.stato.MacchinaStatiPartita;
import it.unife.volley.backend.dto.*;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.model.*;
import it.unife.volley.backend.repository.*;
import it.unife.volley.backend.security.UtenteCorrente;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Referto digitale: compilazione e firma (arbitro), revisione e omologazione (giudice sportivo).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefertoService {

    private final PartitaRepository partitaRepository;
    private final StatisticaGiocatoreRepository statisticaRepository;
    private final ListaGaraRepository listaGaraRepository;
    private final CampionatoRepository campionatoRepository;
    private final PartitaService partitaService;
    private final RefertoValidator validator;
    private final RefertoFactory factory;
    private final MacchinaStatiPartita macchinaStati;
    private final UtenteCorrente utenteCorrente;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper mapper;

    public RefertoDto referto(Long idPartita) {
        Partita p = partitaService.carica(idPartita);
        if (utenteCorrente.principal().ruolo() == Ruolo.ARBITRO) {
            partitaService.verificaArbitroDesignato(p);
        }
        return new RefertoDto(mapper.partita(p),
                p.getSet().stream().map(mapper::set).toList(),
                statisticaRepository.findByPartitaId(idPartita).stream().map(mapper::statistica).toList(),
                listaGaraRepository.findByPartita(idPartita).stream().map(mapper::lista).toList(),
                p.getNoteReferto(), p.getHashReferto());
    }

    /**
     * Salvataggio atomico del referto firmato.
     * <p>
     * I parziali vengono scritti e sincronizzati sul DB <b>prima</b> delle statistiche: se una qualsiasi
     * statistica fallisce (es. atleta non in lista gara) l'eccezione provoca il rollback dell'intera
     * transazione, quindi nessun set, nessuna statistica e nessun cambio di stato restano persistiti.
     * La classifica, calcolata solo sulle partite omologate, non puo' quindi mai riflettere un referto parziale.
     */
    @Transactional(rollbackFor = Exception.class)
    public RefertoDto inviaReferto(Long idPartita, RefertoRequest req) {
        Partita p = partitaService.carica(idPartita);
        partitaService.verificaArbitroDesignato(p);
        // verifica anticipata della transizione IN_CORSO -> REFERTATA (lancia 409 se non ammessa)
        macchinaStati.statoDi(p).inviaReferto();

        Utente arbitro = utenteCorrente.entita();
        if (!passwordEncoder.matches(req.passwordFirma(), arbitro.getPassword())) {
            throw new BusinessException("Firma non valida: password errata");
        }

        EsitoPartita esito = validator.valida(req.set());
        List<String> erroriStat = validator.verificaPrestazioni(req.statistiche());
        if (!erroriStat.isEmpty()) {
            throw new BusinessException("Statistiche non valide", erroriStat);
        }

        // sostituzione di un eventuale referto respinto in precedenza
        statisticaRepository.deleteByPartitaId(idPartita);
        p.getSet().clear();
        partitaRepository.flush();

        req.set().forEach(s -> p.getSet().add(factory.creaSet(p, s)));
        partitaRepository.saveAndFlush(p);

        Map<Long, Tesserato> convocati = listaGaraRepository.findByPartita(idPartita).stream()
                .flatMap(l -> l.getAtleti().stream())
                .collect(Collectors.toMap(Tesserato::getId, Function.identity()));
        for (PrestazioneAtleta prestazione : req.statistiche()) {
            Tesserato t = convocati.get(prestazione.idTesserato());
            if (t == null) {
                throw new BusinessException("L'atleta " + prestazione.idTesserato() + " non e' presente in nessuna lista gara");
            }
            statisticaRepository.save(factory.creaStatistica(p, t, prestazione));
        }

        p.setSetCasa(esito.setCasa());
        p.setSetOspite(esito.setOspite());
        p.setNoteReferto(req.note());
        p.setNoteGiudice(null);
        p.setFirmaArbitroAt(LocalDateTime.now());
        p.setHashReferto(factory.impronta(p, req.set(), req.statistiche(), arbitro.getEmail()));
        macchinaStati.applica(p, EventoPartita.INVIA_REFERTO);
        return referto(idPartita);
    }

    /** REFERTATA -> OMOLOGATA. Se tutte le gare del campionato sono omologate, il campionato si conclude. */
    @Transactional
    public PartitaDto omologa(Long idPartita) {
        Partita p = partitaService.carica(idPartita);
        macchinaStati.applica(p, EventoPartita.OMOLOGA);
        p.setOmologataAt(LocalDateTime.now());
        partitaRepository.flush();
        Campionato c = p.getCampionato();
        boolean tutteOmologate = partitaRepository.findByCampionato(c.getId()).stream()
                .allMatch(x -> x.getStato() == StatoPartita.OMOLOGATA);
        if (tutteOmologate) {
            c.setStato(StatoCampionato.CONCLUSO);
            campionatoRepository.save(c);
        }
        return mapper.partita(p);
    }

    /** REFERTATA -> IN_CORSO: il referto torna all'arbitro con la motivazione del giudice. */
    @Transactional
    public PartitaDto respingi(Long idPartita, String motivazione) {
        Partita p = partitaService.carica(idPartita);
        macchinaStati.applica(p, EventoPartita.RESPINGI);
        p.setNoteGiudice(motivazione.trim());
        p.setFirmaArbitroAt(null);
        p.setHashReferto(null);
        return mapper.partita(p);
    }
}
