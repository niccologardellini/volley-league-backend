package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.Squadra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** Accesso alla tabella squadra; le query caricano subito societa' e campionato per evitare query aggiuntive. */
public interface SquadraRepository extends JpaRepository<Squadra, Long> {

    @Query("select s from Squadra s join fetch s.societa left join fetch s.campionato where s.campionato.id = :idCampionato order by s.nome")
    List<Squadra> findByCampionatoId(Long idCampionato);

    @Query("select s from Squadra s join fetch s.societa left join fetch s.campionato where s.societa.id = :idSocieta order by s.nome")
    List<Squadra> findBySocietaId(Long idSocieta);
}
