package it.unife.volley.backend.model;

import jakarta.persistence.*;
import lombok.*;

/** Societa' sportiva affiliata FIPAV. */
@Entity
@Table(name = "societa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Societa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Codice di affiliazione federale, univoco. */
    @Column(name = "codice_fipav", nullable = false, unique = true, length = 20)
    private String codiceFipav;

    @Column(nullable = false, length = 60)
    private String citta;

    /** Sigla provincia (es. FE, BO): usata dai vincoli di designazione arbitrale. */
    @Column(nullable = false, length = 2)
    private String provincia;
}
