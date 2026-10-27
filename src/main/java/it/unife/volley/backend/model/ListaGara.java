package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lista gara (elenco dei convocati) inviata da una squadra per una partita.
 * La relazione N:M con i tesserati e' mappata sulla tabella {@code lista_gara_atleta}.
 */
@Entity
@Table(name = "lista_gara",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_partita", "id_squadra"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListaGara {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_partita", nullable = false)
    private Partita partita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_squadra", nullable = false)
    private Squadra squadra;

    @ManyToMany
    @JoinTable(name = "lista_gara_atleta",
            joinColumns = @JoinColumn(name = "id_lista_gara"),
            inverseJoinColumns = @JoinColumn(name = "id_tesserato"))
    @Builder.Default
    private Set<Tesserato> atleti = new LinkedHashSet<>();

    @Column(name = "allenatore", nullable = false, length = 120)
    private String allenatore;

    @Column(name = "vice_allenatore", length = 120)
    private String viceAllenatore;

    @Column(name = "dirigente_accompagnatore", length = 120)
    private String dirigenteAccompagnatore;

    @Column(name = "inviata_at", nullable = false)
    private LocalDateTime inviataAt;
}
