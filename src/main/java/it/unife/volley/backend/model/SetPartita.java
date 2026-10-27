package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Parziale di un singolo set. Per ogni partita il numero del set e' univoco (1..5). */
@Entity
@Table(name = "set_partita",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_partita", "numero_set"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SetPartita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_partita", nullable = false)
    private Partita partita;

    @Column(name = "numero_set", nullable = false)
    private Integer numeroSet;

    @Column(name = "punti_casa", nullable = false)
    private Integer puntiCasa;

    @Column(name = "punti_ospite", nullable = false)
    private Integer puntiOspite;

    /** Sostituzioni effettuate nel set dalla squadra di casa (max 6). */
    @Column(name = "cambi_casa", nullable = false)
    private Integer cambiCasa;

    @Column(name = "cambi_ospite", nullable = false)
    private Integer cambiOspite;
}
