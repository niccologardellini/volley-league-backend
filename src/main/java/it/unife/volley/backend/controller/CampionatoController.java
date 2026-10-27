package it.unife.volley.backend.controller;

import it.unife.volley.backend.domain.classifica.CriterioOrdinamento;
import it.unife.volley.backend.domain.classifica.RigaClassifica;
import it.unife.volley.backend.dto.*;
import it.unife.volley.backend.service.CampionatoService;
import it.unife.volley.backend.service.ClassificaService;
import it.unife.volley.backend.service.PartitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Campionati, iscrizioni, calendario e classifica. Lettura consentita a tutti gli utenti autenticati. */
@RestController
@RequestMapping("/api/campionati")
@RequiredArgsConstructor
public class CampionatoController {

    private final CampionatoService campionatoService;
    private final PartitaService partitaService;
    private final ClassificaService classificaService;

    @GetMapping
    public List<CampionatoDto> elenco() {
        return campionatoService.elenco();
    }

    @GetMapping("/{id}")
    public CampionatoDto dettaglio(@PathVariable Long id) {
        return campionatoService.dettaglio(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CampionatoDto crea(@Valid @RequestBody CampionatoRequest req) {
        return campionatoService.crea(req);
    }

    @GetMapping("/{id}/squadre")
    public List<SquadraDto> squadre(@PathVariable Long id) {
        return campionatoService.squadre(id);
    }

    @PostMapping("/{id}/iscrizioni")
    @PreAuthorize("hasRole('DIRIGENTE')")
    public SquadraDto iscrivi(@PathVariable Long id, @Valid @RequestBody IscrizioneRequest req) {
        return campionatoService.iscrivi(id, req.idSquadra());
    }

    @PostMapping("/{id}/calendario")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PartitaDto> generaCalendario(@PathVariable Long id, @Valid @RequestBody GenerazioneCalendarioRequest req) {
        return campionatoService.generaCalendario(id, req);
    }

    @GetMapping("/{id}/partite")
    public List<PartitaDto> calendario(@PathVariable Long id) {
        return partitaService.calendario(id);
    }

    @GetMapping("/{id}/classifica")
    public List<RigaClassifica> classifica(@PathVariable Long id,
                                           @RequestParam(defaultValue = "FIPAV") CriterioOrdinamento criterio) {
        return classificaService.classifica(id, criterio);
    }
}
