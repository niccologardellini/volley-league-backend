package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.Partita;
import it.unife.volley.backend.model.StatoPartita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PartitaRepository extends JpaRepository<Partita, Long> {

    String FETCH = "select distinct p from Partita p join fetch p.campionato " +
            "join fetch p.squadraCasa sc join fetch sc.societa " +
            "join fetch p.squadraOspite so join fetch so.societa " +
            "left join fetch p.arbitro ";

    @Query(FETCH + "where p.campionato.id = :idCampionato order by p.giornata, p.dataOra")
    List<Partita> findByCampionato(Long idCampionato);

    @Query(FETCH + "where p.arbitro.id = :idArbitro order by p.dataOra")
    List<Partita> findByArbitro(Long idArbitro);

    @Query(FETCH + "where sc.id = :idSquadra or so.id = :idSquadra order by p.dataOra")
    List<Partita> findBySquadra(Long idSquadra);

    @Query(FETCH + "where p.stato in :stati order by p.dataOra")
    List<Partita> findByStati(Collection<StatoPartita> stati);

    @Query("select p from Partita p where p.campionato.id = :idCampionato and p.stato = :stato")
    List<Partita> findByCampionatoAndStato(Long idCampionato, StatoPartita stato);

    long countByCampionatoId(Long idCampionato);

    /** Partite gia' assegnate all'arbitro in una finestra temporale (vincolo di disponibilita'). */
    @Query("select p from Partita p where p.arbitro.id = :idArbitro and p.id <> :idEscludi " +
            "and p.dataOra between :da and :a")
    List<Partita> findImpegniArbitro(Long idArbitro, Long idEscludi, LocalDateTime da, LocalDateTime a);
}
