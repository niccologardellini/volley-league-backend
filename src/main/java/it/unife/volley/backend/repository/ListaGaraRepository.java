package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.ListaGara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ListaGaraRepository extends JpaRepository<ListaGara, Long> {

    @Query("select distinct l from ListaGara l left join fetch l.atleti where l.partita.id = :idPartita and l.squadra.id = :idSquadra")
    Optional<ListaGara> findByPartitaAndSquadra(Long idPartita, Long idSquadra);

    @Query("select distinct l from ListaGara l left join fetch l.atleti join fetch l.squadra where l.partita.id = :idPartita")
    List<ListaGara> findByPartita(Long idPartita);

    long countByPartitaId(Long idPartita);

    /** Liste gara in cui compare l'atleta (storico convocazioni). */
    @Query("select l from ListaGara l join l.atleti a join fetch l.partita p join fetch p.squadraCasa join fetch p.squadraOspite " +
            "where a.id = :idTesserato order by p.dataOra")
    List<ListaGara> findByAtleta(Long idTesserato);
}
