package it.unife.volley.backend.domain.referto;

import it.unife.volley.backend.model.*;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/**
 * Factory Pattern: centralizza la costruzione delle entita' che compongono un referto
 * (parziali e statistiche) e dell'impronta digitale usata per la firma.
 * I servizi non istanziano mai direttamente {@link SetPartita} o {@link StatisticaGiocatore}.
 */
@Component
public class RefertoFactory {

    public SetPartita creaSet(Partita partita, ParzialeSet parziale) {
        return SetPartita.builder()
                .partita(partita)
                .numeroSet(parziale.numeroSet())
                .puntiCasa(parziale.puntiCasa())
                .puntiOspite(parziale.puntiOspite())
                .cambiCasa(parziale.cambiCasa())
                .cambiOspite(parziale.cambiOspite())
                .build();
    }

    public StatisticaGiocatore creaStatistica(Partita partita, Tesserato tesserato, PrestazioneAtleta p) {
        return StatisticaGiocatore.builder()
                .partita(partita)
                .tesserato(tesserato)
                .puntiFatti(p.puntiFatti())
                .ace(p.ace())
                .muri(p.muri())
                .errori(p.errori())
                .cartellino(p.cartellino() == null ? Sanzione.NESSUNA : p.cartellino())
                .build();
    }

    /**
     * Impronta SHA-256 del contenuto del referto: partita, parziali e statistiche in forma canonica.
     * Qualsiasi modifica successiva alla firma produce un hash diverso.
     */
    public String impronta(Partita partita, List<ParzialeSet> parziali, List<PrestazioneAtleta> prestazioni, String emailArbitro) {
        StringBuilder sb = new StringBuilder("partita=").append(partita.getId()).append(";arbitro=").append(emailArbitro);
        parziali.stream().sorted(Comparator.comparing(ParzialeSet::numeroSet)).forEach(s ->
                sb.append(";S").append(s.numeroSet()).append('=').append(s.puntiCasa()).append('-').append(s.puntiOspite())
                        .append('/').append(s.cambiCasa()).append('-').append(s.cambiOspite()));
        prestazioni.stream().sorted(Comparator.comparing(PrestazioneAtleta::idTesserato)).forEach(p ->
                sb.append(";A").append(p.idTesserato()).append('=').append(p.puntiFatti()).append(',').append(p.ace())
                        .append(',').append(p.muri()).append(',').append(p.errori()).append(',').append(p.cartellino()));
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(sb.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 non disponibile", e);
        }
    }
}
