package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.classifica.CalcolatoreClassifica;
import it.unife.volley.backend.domain.classifica.CriterioOrdinamento;
import it.unife.volley.backend.domain.classifica.OrdinamentoClassificaStrategy;
import it.unife.volley.backend.domain.classifica.RigaClassifica;
import it.unife.volley.backend.model.StatoPartita;
import it.unife.volley.backend.repository.PartitaRepository;
import it.unife.volley.backend.repository.SetPartitaRepository;
import it.unife.volley.backend.repository.SquadraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Classifica di un campionato. Le strategie di ordinamento disponibili vengono raccolte
 * automaticamente da Spring (tutti i bean {@link OrdinamentoClassificaStrategy}).
 */
@Service
@Transactional(readOnly = true)
public class ClassificaService {

    private final SquadraRepository squadraRepository;
    private final PartitaRepository partitaRepository;
    private final SetPartitaRepository setPartitaRepository;
    private final CalcolatoreClassifica calcolatore;
    private final Map<CriterioOrdinamento, OrdinamentoClassificaStrategy> strategie = new EnumMap<>(CriterioOrdinamento.class);

    public ClassificaService(SquadraRepository squadraRepository, PartitaRepository partitaRepository,
                             SetPartitaRepository setPartitaRepository, CalcolatoreClassifica calcolatore,
                             List<OrdinamentoClassificaStrategy> strategie) {
        this.squadraRepository = squadraRepository;
        this.partitaRepository = partitaRepository;
        this.setPartitaRepository = setPartitaRepository;
        this.calcolatore = calcolatore;
        strategie.forEach(s -> this.strategie.put(s.criterio(), s));
    }

    public List<RigaClassifica> classifica(Long idCampionato, CriterioOrdinamento criterio) {
        OrdinamentoClassificaStrategy strategia = strategie.get(criterio == null ? CriterioOrdinamento.FIPAV : criterio);
        return calcolatore.calcola(
                squadraRepository.findByCampionatoId(idCampionato),
                partitaRepository.findByCampionatoAndStato(idCampionato, StatoPartita.OMOLOGATA),
                setPartitaRepository.findByCampionatoAndStato(idCampionato, StatoPartita.OMOLOGATA),
                strategia);
    }
}
