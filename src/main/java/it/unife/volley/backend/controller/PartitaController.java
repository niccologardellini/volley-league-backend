package it.unife.volley.backend.controller;

import it.unife.volley.backend.dto.*;
import it.unife.volley.backend.model.StatoPartita;
import it.unife.volley.backend.service.DesignazioneService;
import it.unife.volley.backend.service.ListaGaraService;
import it.unife.volley.backend.service.PartitaService;
import it.unife.volley.backend.service.RefertoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Ciclo di vita della partita: designazione, liste gara, avvio, referto, omologazione. */
@RestController
@RequestMapping("/api/partite")
@RequiredArgsConstructor
public class PartitaController {

    private final PartitaService partitaService;
    private final DesignazioneService designazioneService;
    private final ListaGaraService listaGaraService;
    private final RefertoService refertoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PartitaDto> perStato(@RequestParam(required = false) List<StatoPartita> stato) {
        return partitaService.perStato(stato);
    }

    @GetMapping("/{id}")
    public PartitaDto dettaglio(@PathVariable Long id) {
        return partitaService.dettaglio(id);
    }

    // ---- Designazioni (giudice sportivo) ----

    @GetMapping("/{id}/arbitri")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ArbitroCompatibileDto> arbitri(@PathVariable Long id) {
        return designazioneService.arbitriPerPartita(id);
    }

    @PutMapping("/{id}/arbitro")
    @PreAuthorize("hasRole('ADMIN')")
    public PartitaDto designa(@PathVariable Long id, @Valid @RequestBody DesignazioneRequest req) {
        return designazioneService.designa(id, req.idArbitro());
    }

    // ---- Liste gara (dirigente) ----

    @GetMapping("/{id}/liste/{idSquadra}")
    @PreAuthorize("hasAnyRole('DIRIGENTE','ADMIN')")
    public ResponseEntity<ListaGaraDto> lista(@PathVariable Long id, @PathVariable Long idSquadra) {
        return listaGaraService.lista(id, idSquadra).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}/liste/{idSquadra}")
    @PreAuthorize("hasRole('DIRIGENTE')")
    public ListaGaraDto inviaLista(@PathVariable Long id, @PathVariable Long idSquadra, @Valid @RequestBody ListaGaraRequest req) {
        return listaGaraService.invia(id, idSquadra, req);
    }

    // ---- Arbitro ----

    @GetMapping("/designazioni/mie")
    @PreAuthorize("hasRole('ARBITRO')")
    public List<PartitaDto> mieDesignazioni() {
        return partitaService.mieDesignazioni();
    }

    @PostMapping("/{id}/avvio")
    @PreAuthorize("hasRole('ARBITRO')")
    public PartitaDto avvia(@PathVariable Long id) {
        return partitaService.avviaGara(id);
    }

    @GetMapping("/{id}/referto")
    @PreAuthorize("hasAnyRole('ARBITRO','ADMIN')")
    public RefertoDto referto(@PathVariable Long id) {
        return refertoService.referto(id);
    }

    @PostMapping("/{id}/referto")
    @PreAuthorize("hasRole('ARBITRO')")
    public RefertoDto inviaReferto(@PathVariable Long id, @Valid @RequestBody RefertoRequest req) {
        return refertoService.inviaReferto(id, req);
    }

    // ---- Omologazione (giudice sportivo) ----

    @PostMapping("/{id}/omologa")
    @PreAuthorize("hasRole('ADMIN')")
    public PartitaDto omologa(@PathVariable Long id) {
        return refertoService.omologa(id);
    }

    @PostMapping("/{id}/respingi")
    @PreAuthorize("hasRole('ADMIN')")
    public PartitaDto respingi(@PathVariable Long id, @Valid @RequestBody MotivazioneRequest req) {
        return refertoService.respingi(id, req.motivazione());
    }
}
