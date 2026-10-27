package it.unife.volley.backend.service;

import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.dto.LoginResponse;
import it.unife.volley.backend.dto.UtenteDto;
import it.unife.volley.backend.model.Utente;
import it.unife.volley.backend.repository.UtenteRepository;
import it.unife.volley.backend.security.JwtService;
import it.unife.volley.backend.security.UtenteCorrente;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Autenticazione con email e password ed emissione del token JWT. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UtenteCorrente utenteCorrente;
    private final DtoMapper mapper;

    public LoginResponse login(String email, String password) {
        Utente u = utenteRepository.findByEmailIgnoreCase(email.trim())
                .filter(x -> passwordEncoder.matches(password, x.getPassword()))
                // messaggio generico: non rivela se l'email esiste
                .orElseThrow(() -> new BadCredentialsException("Email o password non corretti"));
        return new LoginResponse(jwtService.genera(u), mapper.utente(u));
    }

    public UtenteDto me() {
        return mapper.utente(utenteCorrente.entita());
    }
}
