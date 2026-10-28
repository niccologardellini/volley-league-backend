package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.Campionato;
import it.unife.volley.backend.model.StatoCampionato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Accesso alla tabella campionato. */
public interface CampionatoRepository extends JpaRepository<Campionato, Long> {

    List<Campionato> findByStatoOrderByNome(StatoCampionato stato);

    List<Campionato> findAllByOrderByStagioneDescNomeAscGironeAsc();
}
