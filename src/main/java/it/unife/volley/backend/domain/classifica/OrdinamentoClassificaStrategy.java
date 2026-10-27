package it.unife.volley.backend.domain.classifica;

import java.util.Comparator;

/**
 * Strategy Pattern: ogni implementazione definisce un criterio di ordinamento della classifica.
 * Il comparatore ordina dalla prima all'ultima posizione.
 */
public interface OrdinamentoClassificaStrategy {

    CriterioOrdinamento criterio();

    Comparator<RigaClassifica> comparatore();

    /** Spareggi comuni: quoziente set, poi quoziente punti, infine nome (per un ordine deterministico). */
    static Comparator<RigaClassifica> spareggiQuozienti() {
        Comparator<RigaClassifica> qs = (a, b) -> Quozienti.confronta(b.getSetVinti(), b.getSetPersi(), a.getSetVinti(), a.getSetPersi());
        Comparator<RigaClassifica> qp = (a, b) -> Quozienti.confronta(b.getPuntiFatti(), b.getPuntiSubiti(), a.getPuntiFatti(), a.getPuntiSubiti());
        return qs.thenComparing(qp).thenComparing(RigaClassifica::getSquadra);
    }
}
