package it.unife.volley.backend.domain.calendario;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generazione del calendario all'italiana con l'algoritmo di Berger (metodo del cerchio).
 * <p>
 * Con N squadre (N pari; se dispari si aggiunge un "riposo") si ottengono N-1 giornate di andata
 * in cui ogni squadra gioca esattamente una volta e incontra tutte le altre una sola volta.
 * Il girone di ritorno ripete le giornate invertendo il campo.
 * La squadra fissa alterna casa/trasferta per bilanciare gli incontri casalinghi.
 */
@Component
public class GeneratoreCalendario {

    public <T> List<Accoppiamento<T>> genera(List<T> squadre, LocalDate dataInizio, LocalTime ora,
                                             int giorniTraGiornate, boolean andataRitorno) {
        if (squadre == null || squadre.size() < 2) {
            throw new IllegalArgumentException("Servono almeno 2 squadre per generare il calendario");
        }
        List<T> cerchio = new ArrayList<>(squadre);
        if (cerchio.size() % 2 != 0) {
            cerchio.add(null); // null = turno di riposo
        }
        int n = cerchio.size();
        int giornateAndata = n - 1;
        List<Accoppiamento<T>> risultato = new ArrayList<>();

        for (int g = 0; g < giornateAndata; g++) {
            LocalDate dataAndata = dataInizio.plusDays((long) g * giorniTraGiornate);
            LocalDate dataRitorno = dataInizio.plusDays((long) (g + giornateAndata) * giorniTraGiornate);
            for (int i = 0; i < n / 2; i++) {
                T a = cerchio.get(i);
                T b = cerchio.get(n - 1 - i);
                if (a == null || b == null) continue;
                // alterna il campo della squadra fissa (indice 0) e delle coppie in base alla giornata
                boolean inverti = (i == 0) ? (g % 2 == 1) : (i % 2 == 1);
                T casa = inverti ? b : a;
                T ospite = inverti ? a : b;
                risultato.add(new Accoppiamento<>(g + 1, casa, ospite, dataAndata.atTime(ora)));
                if (andataRitorno) {
                    risultato.add(new Accoppiamento<>(g + 1 + giornateAndata, ospite, casa, dataRitorno.atTime(ora)));
                }
            }
            // rotazione: il primo elemento resta fisso, l'ultimo passa in seconda posizione
            T ultimo = cerchio.remove(n - 1);
            cerchio.add(1, ultimo);
        }
        risultato.sort((x, y) -> Integer.compare(x.giornata(), y.giornata()));
        return risultato;
    }
}
