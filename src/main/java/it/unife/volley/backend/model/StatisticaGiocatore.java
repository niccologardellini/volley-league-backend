package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Prestazione individuale di un atleta in una partita, registrata sul referto. */
@Entity
@Table(name = "statistica_giocatore",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_partita", "id_tesserato"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatisticaGiocatore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_partita", nullable = false)
    private Partita partita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tesserato", nullable = false)
    private Tesserato tesserato;

    @Column(name = "punti_fatti", nullable = false)
    private Integer puntiFatti;

    @Column(nullable = false)
    private Integer ace;

    @Column(nullable = false)
    private Integer muri;

    @Column(nullable = false)
    private Integer errori;

    @Enumerated(EnumType.STRING)
    @Column(name = "cartellino", nullable = false, length = 20)
    private Sanzione cartellino;
}
