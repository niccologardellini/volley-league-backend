package it.unife.volley.backend.security;

import it.unife.volley.backend.exception.NotFoundException;
import it.unife.volley.backend.model.Utente;
import it.unife.volley.backend.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Accesso all'utente autenticato della richiesta corrente. */
@Component
@RequiredArgsConstructor
public class UtenteCorrente {

    private final UtenteRepository utenteRepository;

    public UtenteAutenticato principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UtenteAutenticato u)) {
            throw new InsufficientAuthenticationException("Nessun utente autenticato");
        }
        return u;
    }

    public Utente entita() {
        Long id = principal().id();
        return utenteRepository.findById(id).orElseThrow(() -> new NotFoundException("Utente", id));
    }
}
