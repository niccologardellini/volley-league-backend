package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.SetPartita;
import it.unife.volley.backend.model.StatoPartita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SetPartitaRepository extends JpaRepository<SetPartita, Long> {

    List<SetPartita> findByPartitaIdOrderByNumeroSet(Long idPartita);

    /** Tutti i parziali delle partite di un campionato in un certo stato (usato dalla classifica). */
    @Query("select s from SetPartita s join fetch s.partita p where p.campionato.id = :idCampionato and p.stato = :stato")
    List<SetPartita> findByCampionatoAndStato(Long idCampionato, StatoPartita stato);
}
