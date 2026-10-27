package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Squadra di una societa'; e' iscritta al piu' a un campionato. */
@Entity
@Table(name = "squadra")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Squadra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_societa", nullable = false)
    private Societa societa;

    /** Null finche' la squadra non viene iscritta a un campionato. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_campionato")
    private Campionato campionato;
}
