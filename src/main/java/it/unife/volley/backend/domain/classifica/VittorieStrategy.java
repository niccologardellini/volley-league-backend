package it.unife.volley.backend.domain.classifica;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/** Ordinamento per numero di vittorie, poi punti e quozienti. */
@Component
public class VittorieStrategy implements OrdinamentoClassificaStrategy {

    @Override
    public CriterioOrdinamento criterio() {
        return CriterioOrdinamento.VITTORIE;
    }

    @Override
    public Comparator<RigaClassifica> comparatore() {
        return Comparator.comparingInt(RigaClassifica::getVinte).reversed()
                .thenComparing(Comparator.comparingInt(RigaClassifica::getPunti).reversed())
                .thenComparing(OrdinamentoClassificaStrategy.spareggiQuozienti());
    }
}
