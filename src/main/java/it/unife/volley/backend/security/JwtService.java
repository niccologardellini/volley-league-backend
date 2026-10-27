package it.unife.volley.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Utente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** Emissione e verifica dei token JWT firmati HMAC-SHA256. */
@Service
public class JwtService {

    private final SecretKey chiave;
    private final long durataMinuti;

    public JwtService(@Value("${app.jwt.secret}") String segreto,
                      @Value("${app.jwt.expiration-minutes}") long durataMinuti) {
        this.chiave = Keys.hmacShaKeyFor(segreto.getBytes(StandardCharsets.UTF_8));
        this.durataMinuti = durataMinuti;
    }

    public String genera(Utente utente) {
        Instant ora = Instant.now();
        return Jwts.builder()
                .subject(utente.getEmail())
                .claim("uid", utente.getId())
                .claim("ruolo", utente.getRuolo().name())
                .issuedAt(Date.from(ora))
                .expiration(Date.from(ora.plus(durataMinuti, ChronoUnit.MINUTES)))
                .signWith(chiave)
                .compact();
    }

    /** @return il principal se il token e' integro e non scaduto, altrimenti vuoto */
    public Optional<UtenteAutenticato> verifica(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(chiave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UtenteAutenticato(
                    c.get("uid", Number.class).longValue(),
                    c.getSubject(),
                    Ruolo.valueOf(c.get("ruolo", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
