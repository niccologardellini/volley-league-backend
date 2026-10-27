package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Atleta tesserato per una squadra. Il numero di maglia e' univoco all'interno della squadra. */
@Entity
@Table(name = "tesserato",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_squadra", "numero_maglia"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tesserato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 60)
    private String cognome;

    @Enumerated(EnumType.STRING)
    @Column(name = "ruolo_campo", nullable = false, length = 20)
    private RuoloCampo ruoloCampo;

    @Column(name = "numero_maglia", nullable = false)
    private Integer numeroMaglia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_squadra", nullable = false)
    private Squadra squadra;

    /** Account del giocatore per l'area personale (facoltativo). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utente", unique = true)
    private Utente utente;
}
