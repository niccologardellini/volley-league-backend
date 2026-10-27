package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.StatisticaGiocatore;
import it.unife.volley.backend.model.StatoPartita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StatisticaGiocatoreRepository extends JpaRepository<StatisticaGiocatore, Long> {

    @Query("select s from StatisticaGiocatore s join fetch s.tesserato t join fetch t.squadra where s.partita.id = :idPartita")
    List<StatisticaGiocatore> findByPartitaId(Long idPartita);

    @Query("select s from StatisticaGiocatore s join fetch s.partita p join fetch p.squadraCasa join fetch p.squadraOspite " +
            "where s.tesserato.id = :idTesserato and p.stato = :stato order by p.dataOra")
    List<StatisticaGiocatore> findByTesseratoAndStato(Long idTesserato, StatoPartita stato);

    /**
     * Totali stagionali di un atleta sulle sole partite omologate (query aggregata).
     * Restituisce: [presenze, punti, ace, muri, errori].
     */
    @Query("select count(s), coalesce(sum(s.puntiFatti),0), coalesce(sum(s.ace),0), coalesce(sum(s.muri),0), coalesce(sum(s.errori),0) " +
            "from StatisticaGiocatore s where s.tesserato.id = :idTesserato and s.partita.stato = :stato")
    List<Object[]> totaliStagionali(Long idTesserato, StatoPartita stato);

    void deleteByPartitaId(Long idPartita);
}
