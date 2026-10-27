package it.unife.volley.backend.controller;

import it.unife.volley.backend.dto.LoginRequest;
import it.unife.volley.backend.dto.LoginResponse;
import it.unife.volley.backend.dto.UtenteDto;
import it.unife.volley.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Pubblico: restituisce il token JWT e il profilo dell'utente. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.email(), req.password());
    }

    /** Profilo dell'utente autenticato (usato dal frontend al refresh della pagina). */
    @GetMapping("/me")
    public UtenteDto me() {
        return authService.me();
    }
}
