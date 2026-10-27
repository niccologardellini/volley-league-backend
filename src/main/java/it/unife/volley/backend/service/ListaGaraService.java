package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.listagara.ListaGaraValidator;
import it.unife.volley.backend.domain.stato.EventoPartita;
import it.unife.volley.backend.domain.stato.MacchinaStatiPartita;
import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.ListaGaraDto;
import it.unife.volley.backend.dto.ListaGaraRequest;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.exception.TransizioneNonValidaException;
import it.unife.volley.backend.model.ListaGara;
import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.Squadra;
import it.unife.volley.backend.model.Tesserato;
import it.unife.volley.backend.repository.ListaGaraRepository;
import it.unife.volley.backend.repository.TesseratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Compilazione e invio della lista gara da parte del dirigente. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListaGaraService {

    private final ListaGaraRepository listaGaraRepository;
    private final TesseratoRepository tesseratoRepository;
    private final PartitaService partitaService;
    private final SquadraService squadraService;
    private final ListaGaraValidator validator;
    private final MacchinaStatiPartita macchinaStati;
    private final DtoMapper mapper;

    public Optional<ListaGaraDto> lista(Long idPartita, Long idSquadra) {
        squadraService.caricaConProprieta(idSquadra);
        return listaGaraRepository.findByPartitaAndSquadra(idPartita, idSquadra).map(mapper::lista);
    }

    /**
     * Invia (o sostituisce) la lista gara. Quando entrambe le squadre l'hanno inviata
     * la partita passa a ROSTER_INVIATI.
     */
    @Transactional
    public ListaGaraDto invia(Long idPartita, Long idSquadra, ListaGaraRequest req) {
        Squadra squadra = squadraService.caricaConProprieta(idSquadra);
        Partita partita = partitaService.carica(idPartita);
        if (!Objects.equals(partita.getSquadraCasa().getId(), idSquadra) && !Objects.equals(partita.getSquadraOspite().getId(), idSquadra)) {
            throw new BusinessException(squadra.getNome() + " non partecipa a questa partita");
        }
        if (!macchinaStati.statoDi(partita).listaGaraModificabile()) {
            throw new TransizioneNonValidaException(partita.getStato(), "invio lista gara");
        }
        List<Tesserato> atleti = tesseratoRepository.findAllById(req.atleti());
        List<String> errori = validator.verifica(squadra, req.atleti(), atleti);
        if (!errori.isEmpty()) {
            throw new BusinessException("Lista gara non conforme", errori);
        }

        ListaGara lista = listaGaraRepository.findByPartitaAndSquadra(idPartita, idSquadra)
                .orElseGet(() -> ListaGara.builder().partita(partita).squadra(squadra).build());
        lista.setAtleti(new LinkedHashSet<>(atleti));
        lista.setAllenatore(req.allenatore().trim());
        lista.setViceAllenatore(blankToNull(req.viceAllenatore()));
        lista.setDirigenteAccompagnatore(blankToNull(req.dirigenteAccompagnatore()));
        lista.setInviataAt(LocalDateTime.now());
        listaGaraRepository.saveAndFlush(lista);

        if (listaGaraRepository.countByPartitaId(idPartita) == 2) {
            macchinaStati.applica(partita, EventoPartita.ROSTER_COMPLETATI);
        }
        return mapper.lista(lista);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
