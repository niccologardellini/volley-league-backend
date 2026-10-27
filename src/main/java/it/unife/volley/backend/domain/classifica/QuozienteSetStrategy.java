package it.unife.volley.backend.domain.classifica;

import org.springframework.stereotype.Component;

import java.util.Comparator;

/** Ordinamento per efficienza: quoziente set, quoziente punti. */
@Component
public class QuozienteSetStrategy implements OrdinamentoClassificaStrategy {

    @Override
    public CriterioOrdinamento criterio() {
        return CriterioOrdinamento.QUOZIENTE_SET;
    }

    @Override
    public Comparator<RigaClassifica> comparatore() {
        return OrdinamentoClassificaStrategy.spareggiQuozienti();
    }
}
