package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.Ruolo;
import it.unife.volley.backend.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** Accesso alla tabella utente; la ricerca per email ignora maiuscole e minuscole. */
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from Utente u left join fetch u.societa where u.ruolo = :ruolo order by u.cognome")
    List<Utente> findByRuolo(Ruolo ruolo);
}
