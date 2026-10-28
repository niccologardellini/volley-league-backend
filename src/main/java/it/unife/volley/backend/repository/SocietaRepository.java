package it.unife.volley.backend.repository;

import it.unife.volley.backend.model.Societa;
import org.springframework.data.jpa.repository.JpaRepository;

/** Accesso alla tabella societa (operazioni CRUD standard di Spring Data). */
public interface SocietaRepository extends JpaRepository<Societa, Long> {
}
