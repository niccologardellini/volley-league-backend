package it.unife.volley.backend.dto;

import it.unife.volley.backend.model.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/** Conversione entita' JPA -> DTO esposti dalle API (evita di serializzare relazioni lazy). */
@Component
public class DtoMapper {

    public UtenteDto utente(Utente u) {
        Societa s = u.getSocieta();
        return new UtenteDto(u.getId(), u.getEmail(), u.getNome(), u.getCognome(), u.getRuolo(),
                s == null ? null : s.getId(), s == null ? null : s.getNome(), u.getProvincia());
    }

    public SquadraRef ref(Squadra s) {
        return new SquadraRef(s.getId(), s.getNome(), s.getSocieta().getNome());
    }

    public SquadraDto squadra(Squadra s, long numeroTesserati) {
        Campionato c = s.getCampionato();
        return new SquadraDto(s.getId(), s.getNome(), s.getSocieta().getId(), s.getSocieta().getNome(),
                s.getSocieta().getCitta(), s.getSocieta().getProvincia(),
                c == null ? null : c.getId(), c == null ? null : c.getNome() + " - Girone " + c.getGirone(),
                numeroTesserati);
    }

    public TesseratoDto tesserato(Tesserato t) {
        return new TesseratoDto(t.getId(), t.getNome(), t.getCognome(), t.getRuoloCampo(), t.getNumeroMaglia(),
                t.getSquadra().getId(), t.getUtente() == null ? null : t.getUtente().getEmail());
    }

    public PartitaDto partita(Partita p) {
        List<String> parziali = p.getStato() == StatoPartita.REFERTATA || p.getStato() == StatoPartita.OMOLOGATA
                ? p.getSet().stream().sorted(Comparator.comparing(SetPartita::getNumeroSet))
                    .map(s -> s.getPuntiCasa() + "-" + s.getPuntiOspite()).toList()
                : List.of();
        return new PartitaDto(p.getId(), p.getCampionato().getId(),
                p.getCampionato().getNome() + " - Girone " + p.getCampionato().getGirone(),
                p.getGiornata(), p.getDataOra(), p.getStato(), ref(p.getSquadraCasa()), ref(p.getSquadraOspite()),
                p.getArbitro() == null ? null : p.getArbitro().getId(),
                p.getArbitro() == null ? null : p.getArbitro().getNomeCompleto(),
                p.getSetCasa(), p.getSetOspite(), parziali, p.getFirmaArbitroAt(), p.getNoteGiudice());
    }

    public ListaGaraDto lista(ListaGara l) {
        return new ListaGaraDto(l.getId(), l.getPartita().getId(), l.getSquadra().getId(), l.getSquadra().getNome(),
                l.getAtleti().stream().sorted(Comparator.comparing(Tesserato::getNumeroMaglia)).map(this::tesserato).toList(),
                l.getAllenatore(), l.getViceAllenatore(), l.getDirigenteAccompagnatore(), l.getInviataAt());
    }

    public SetDto set(SetPartita s) {
        return new SetDto(s.getNumeroSet(), s.getPuntiCasa(), s.getPuntiOspite(), s.getCambiCasa(), s.getCambiOspite());
    }

    public StatisticaDto statistica(StatisticaGiocatore s) {
        Tesserato t = s.getTesserato();
        return new StatisticaDto(t.getId(), t.getCognome() + " " + t.getNome(), t.getNumeroMaglia(), t.getSquadra().getId(),
                s.getPuntiFatti(), s.getAce(), s.getMuri(), s.getErrori(), s.getCartellino());
    }
}
