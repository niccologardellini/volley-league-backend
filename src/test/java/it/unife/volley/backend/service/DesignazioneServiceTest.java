package it.unife.volley.backend.service;

import it.unife.volley.backend.domain.designazione.VincoliDesignazione;
import it.unife.volley.backend.domain.stato.MacchinaStatiPartita;
import it.unife.volley.backend.dto.ArbitroCompatibileDto;
import it.unife.volley.backend.dto.DtoMapper;
import it.unife.volley.backend.exception.BusinessException;
import it.unife.volley.backend.exception.TransizioneNonValidaException;
import it.unife.volley.backend.model.*;
import it.unife.volley.backend.repository.PartitaRepository;
import it.unife.volley.backend.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/** Test con Mockito del servizio di designazione: i repository sono simulati. */
@ExtendWith(MockitoExtension.class)
class DesignazioneServiceTest {

    @Mock PartitaRepository partitaRepository;
    @Mock UtenteRepository utenteRepository;
    @Spy VincoliDesignazione vincoli = new VincoliDesignazione();
    @Spy MacchinaStatiPartita macchinaStati = new MacchinaStatiPartita();
    @Spy DtoMapper mapper = new DtoMapper();
    @InjectMocks DesignazioneService service;

    private Partita partita;
    private Utente neutrale, ferrarese;

    @BeforeEach
    void setUp() {
        Societa fe = Societa.builder().id(1L).nome("Estense").provincia("FE").build();
        Societa bo = Societa.builder().id(2L).nome("Due Torri").provincia("BO").build();
        Campionato c = Campionato.builder().id(1L).nome("Serie C").girone("A").build();
        partita = Partita.builder().id(7L).campionato(c).giornata(1)
                .squadraCasa(Squadra.builder().id(1L).nome("Estense Ferrara").societa(fe).build())
                .squadraOspite(Squadra.builder().id(2L).nome("Due Torri Bologna").societa(bo).build())
                .dataOra(LocalDateTime.of(2026, 10, 24, 20, 30)).stato(StatoPartita.PROGRAMMATA).build();
        neutrale = Utente.builder().id(10L).nome("Luca").cognome("Rossi").ruolo(Ruolo.ARBITRO).provincia("RE").build();
        ferrarese = Utente.builder().id(11L).nome("Federica").cognome("Mazza").ruolo(Ruolo.ARBITRO).provincia("FE").build();
        when(partitaRepository.findById(7L)).thenReturn(Optional.of(partita));
    }

    @Test
    void designaArbitroCompatibile() {
        when(utenteRepository.findById(10L)).thenReturn(Optional.of(neutrale));
        when(partitaRepository.findImpegniArbitro(eq(10L), eq(7L), any(), any())).thenReturn(List.of());

        var dto = service.designa(7L, 10L);

        assertThat(partita.getArbitro()).isSameAs(neutrale);
        assertThat(dto.arbitro()).isEqualTo("Luca Rossi");
    }

    @Test
    void rifiutaArbitroDellaStessaProvincia() {
        when(utenteRepository.findById(11L)).thenReturn(Optional.of(ferrarese));
        when(partitaRepository.findImpegniArbitro(anyLong(), anyLong(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.designa(7L, 11L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrori()).anyMatch(m -> m.contains("FE")));
        assertThat(partita.getArbitro()).isNull();
    }

    @Test
    void rifiutaArbitroGiaImpegnato() {
        when(utenteRepository.findById(10L)).thenReturn(Optional.of(neutrale));
        when(partitaRepository.findImpegniArbitro(eq(10L), eq(7L), any(), any())).thenReturn(List.of(new Partita()));

        assertThatThrownBy(() -> service.designa(7L, 10L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void designazioneNonModificabileAGaraIniziata() {
        partita.setStato(StatoPartita.IN_CORSO);
        assertThatThrownBy(() -> service.designa(7L, 10L)).isInstanceOf(TransizioneNonValidaException.class);
    }

    @Test
    void elencoOrdinaICompatibiliPerPrimi() {
        when(utenteRepository.findByRuolo(Ruolo.ARBITRO)).thenReturn(List.of(ferrarese, neutrale));
        when(partitaRepository.findImpegniArbitro(anyLong(), anyLong(), any(), any())).thenReturn(List.of());

        List<ArbitroCompatibileDto> elenco = service.arbitriPerPartita(7L);

        assertThat(elenco).extracting(ArbitroCompatibileDto::nome).containsExactly("Luca Rossi", "Federica Mazza");
        assertThat(elenco.get(1).compatibile()).isFalse();
    }
}
