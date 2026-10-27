package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Incontro di campionato. Il risultato (set vinti) e' denormalizzato in
 * {@code setCasa}/{@code setOspite} per velocizzare il calcolo della classifica;
 * il dettaglio dei parziali e' in {@link SetPartita}.
 */
@Entity
@Table(name = "partita")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Partita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_campionato", nullable = false)
    private Campionato campionato;

    @Column(nullable = false)
    private Integer giornata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_squadra_casa", nullable = false)
    private Squadra squadraCasa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_squadra_ospite", nullable = false)
    private Squadra squadraOspite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_arbitro")
    private Utente arbitro;

    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false, length = 20)
    private StatoPartita stato;

    @Column(name = "set_casa")
    private Integer setCasa;

    @Column(name = "set_ospite")
    private Integer setOspite;

    /** Istante della firma digitale dell'arbitro. */
    @Column(name = "firma_arbitro_at")
    private LocalDateTime firmaArbitroAt;

    /** Impronta SHA-256 del contenuto del referto al momento della firma. */
    @Column(name = "hash_referto", length = 64)
    private String hashReferto;

    @Column(name = "note_referto", length = 1000)
    private String noteReferto;

    /** Motivazione dell'eventuale respingimento da parte del giudice sportivo. */
    @Column(name = "note_giudice", length = 1000)
    private String noteGiudice;

    @Column(name = "omologata_at")
    private LocalDateTime omologataAt;

    @OneToMany(mappedBy = "partita", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroSet ASC")
    @Builder.Default
    private List<SetPartita> set = new ArrayList<>();
}
