package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Campionato (o girone di un campionato) per una data stagione. */
@Entity
@Table(name = "campionato",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nome", "stagione", "girone"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Campionato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Categoria, es. "Serie C Femminile", "Under 18 Maschile". */
    @Column(nullable = false, length = 60)
    private String categoria;

    /** Stagione sportiva nel formato "2026/2027". */
    @Column(nullable = false, length = 9)
    private String stagione;

    @Column(nullable = false, length = 10)
    private String girone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoCampionato stato;
}
