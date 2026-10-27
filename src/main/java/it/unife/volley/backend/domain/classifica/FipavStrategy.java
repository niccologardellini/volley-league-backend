package it.unife.volley.backend.domain.classifica;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/** Ordinamento ufficiale FIPAV: punti, numero di vittorie, quoziente set, quoziente punti. */
@Component
public class FipavStrategy implements OrdinamentoClassificaStrategy {

    @Override
    public CriterioOrdinamento criterio() {
        return CriterioOrdinamento.FIPAV;
    }

    @Override
    public Comparator<RigaClassifica> comparatore() {
        return Comparator.comparingInt(RigaClassifica::getPunti).reversed()
                .thenComparing(Comparator.comparingInt(RigaClassifica::getVinte).reversed())
                .thenComparing(OrdinamentoClassificaStrategy.spareggiQuozienti());
    }
}
