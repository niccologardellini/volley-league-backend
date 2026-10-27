package it.unife.volley.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Account applicativo. Il ruolo determina le funzionalita' accessibili.
 * Per gli arbitri {@code societa} e {@code provincia} rappresentano l'affiliazione
 * usata dai vincoli di designazione; per i dirigenti {@code societa} e' la societa' gestita.
 */
@Entity
@Table(name = "utente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /** Hash BCrypt: la password in chiaro non viene mai salvata. */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 60)
    private String cognome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ruolo ruolo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_societa")
    private Societa societa;

    /** Sigla provincia di residenza (usata per gli arbitri). */
    @Column(length = 2)
    private String provincia;

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}
