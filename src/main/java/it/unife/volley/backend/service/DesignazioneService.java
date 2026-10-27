package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.designazione.VincoliDesignazione;
import it.unife.volley.backend.domain.stato.MacchinaStatiPartita;
import it.unife.volley.backend.dto.ArbitroCompatibileDto;
import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.PartitaDto;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.exception.NotFoundException;
import it.unife.volley.backend.exception.TransizioneNonValidaException;
import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Utente;
import it.unife.volley.backend.repository.PartitaRepository;
import it.unife.volley.backend.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Designazione arbitrale con verifica dei vincoli di compatibilita'. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignazioneService {

    private final PartitaRepository partitaRepository;
    private final UtenteRepository utenteRepository;
    private final VincoliDesignazione vincoli;
    private final MacchinaStatiPartita macchinaStati;
    private final DtoMapper mapper;

    /** Tutti gli arbitri, con l'esito della verifica dei vincoli (compatibili per primi). */
    public List<ArbitroCompatibileDto> arbitriPerPartita(Long idPartita) {
        Partita p = carica(idPartita);
        return utenteRepository.findByRuolo(Ruolo.ARBITRO).stream()
                .map(a -> {
                    List<String> motivi = violazioni(a, p);
                    return new ArbitroCompatibileDto(a.getId(), a.getNomeCompleto(),
                            a.getSocieta() == null ? null : a.getSocieta().getNome(), a.getProvincia(), motivi.isEmpty(), motivi);
                })
                .sorted(Comparator.comparing(ArbitroCompatibileDto::compatibile).reversed()
                        .thenComparing(ArbitroCompatibileDto::nome))
                .toList();
    }

    @Transactional
    public PartitaDto designa(Long idPartita, Long idArbitro) {
        Partita p = carica(idPartita);
        if (!macchinaStati.statoDi(p).designazioneModificabile()) {
            throw new TransizioneNonValidaException(p.getStato(), "designazione arbitro");
        }
        Utente arbitro = utenteRepository.findById(idArbitro).orElseThrow(() -> new NotFoundException("Arbitro", idArbitro));
        List<String> motivi = violazioni(arbitro, p);
        if (!motivi.isEmpty()) {
            throw new BusinessException(arbitro.getNomeCompleto() + " non e' designabile per questa gara", motivi);
        }
        p.setArbitro(arbitro);
        return mapper.partita(p);
    }

    private List<String> violazioni(Utente arbitro, Partita p) {
        boolean impegnato = !partitaRepository.findImpegniArbitro(arbitro.getId(), p.getId(),
                p.getDataOra().toLocalDate().atStartOfDay(), p.getDataOra().toLocalDate().atTime(23, 59, 59)).isEmpty();
        return vincoli.violazioni(arbitro, p.getSquadraCasa(), p.getSquadraOspite(), impegnato);
    }

    private Partita carica(Long id) {
        return partitaRepository.findById(id).orElseThrow(() -> new NotFoundException("Partita", id));
    }
}
