package it.unife.volley.backend.controller;

import it.unife.volley.backend.dto.*;
import it.unife.volley.backend.service.PartitaService;
import it.unife.volley.backend.service.SquadraService;
import it.unife.volley.backend.service.TesseratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Area del dirigente: squadre, rosa (tesseramenti) e partite della squadra. */
@RestController
@RequestMapping("/api/squadre")
@RequiredArgsConstructor
public class SquadraController {

    private final SquadraService squadraService;
    private final TesseratoService tesseratoService;
    private final PartitaService partitaService;

    @GetMapping("/mie")
    @PreAuthorize("hasRole('DIRIGENTE')")
    public List<SquadraDto> mieSquadre() {
        return squadraService.mieSquadre();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DIRIGENTE')")
    public SquadraDto crea(@Valid @RequestBody SquadraRequest req) {
        return squadraService.crea(req);
    }

    @GetMapping("/{id}/tesserati")
    @PreAuthorize("hasAnyRole('DIRIGENTE','ADMIN')")
    public List<TesseratoDto> rosa(@PathVariable Long id) {
        return tesseratoService.rosa(id);
    }

    @PostMapping("/{id}/tesserati")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DIRIGENTE')")
    public TesseratoDto tessera(@PathVariable Long id, @Valid @RequestBody TesseratoRequest req) {
        return tesseratoService.tessera(id, req);
    }

    @PutMapping("/tesserati/{idTesserato}")
    @PreAuthorize("hasRole('DIRIGENTE')")
    public TesseratoDto aggiorna(@PathVariable Long idTesserato, @Valid @RequestBody TesseratoRequest req) {
        return tesseratoService.aggiorna(idTesserato, req);
    }

    @DeleteMapping("/tesserati/{idTesserato}")
    @PreAuthorize("hasRole('DIRIGENTE')")
    public ResponseEntity<Void> svincola(@PathVariable Long idTesserato) {
        tesseratoService.svincola(idTesserato);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/partite")
    @PreAuthorize("hasAnyRole('DIRIGENTE','ADMIN')")
    public List<PartitaDto> partite(@PathVariable Long id) {
        return partitaService.partiteSquadra(id);
    }
}
