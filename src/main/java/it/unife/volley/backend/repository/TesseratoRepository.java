package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.Tesserato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/** Accesso alla tabella tesserato; i controlli sul numero di maglia evitano duplicati nella stessa squadra. */
public interface TesseratoRepository extends JpaRepository<Tesserato, Long> {

    List<Tesserato> findBySquadraIdOrderByNumeroMaglia(Long idSquadra);

    boolean existsBySquadraIdAndNumeroMaglia(Long idSquadra, Integer numeroMaglia);

    boolean existsBySquadraIdAndNumeroMagliaAndIdNot(Long idSquadra, Integer numeroMaglia, Long id);

    @Query("select t from Tesserato t join fetch t.squadra s join fetch s.societa where t.utente.id = :idUtente")
    Optional<Tesserato> findByUtenteId(Long idUtente);
}
