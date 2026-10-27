package it.unife.volley.backend.controller;

import it.unife.volley.backend.dto.AreaGiocatoreDto;
import it.unife.volley.backend.service.GiocatoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/giocatore")
@RequiredArgsConstructor
public class GiocatoreController {

    private final GiocatoreService giocatoreService;

    @GetMapping("/area")
    @PreAuthorize("hasRole('GIOCATORE')")
    public AreaGiocatoreDto area() {
        return giocatoreService.areaPersonale();
    }
}
