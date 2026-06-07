package com.erichiroshi.desafio_itau.application;

import com.erichiroshi.desafio_itau.application.output.EstatisticaOutput;
import com.erichiroshi.desafio_itau.application.port.in.EstatisticaPort;
import com.erichiroshi.desafio_itau.application.port.out.TransacaoRepositoryPort;
import com.erichiroshi.desafio_itau.domain.model.Estatistica;
import com.erichiroshi.desafio_itau.domain.model.Transacao;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EstatisticaUseCase implements EstatisticaPort {

    private final TransacaoRepositoryPort transacaoRepository;
    private final MeterRegistry meterRegistry;

    @Override
    public EstatisticaOutput execute() {
        try {
            return Timer.builder("estatistica.calculo")
                    .description("Tempo de cálculo das estatísticas dos últimos 60s")
                    .tag("tipo", "full")
                    .register(meterRegistry)
                    .recordCallable(this::calcular);
        } catch (Exception ex) {
            log.error("Erro ao calcular estatísticas: {}", ex.getMessage(), ex);
            return EstatisticaOutput.empty();
        }
    }

    private EstatisticaOutput calcular() {
        OffsetDateTime limiteTempo = OffsetDateTime.now().minusSeconds(60);

        log.debug("Buscando transações após: {}", limiteTempo);
        List<Transacao> list = transacaoRepository.findAllAfter(limiteTempo);

        Estatistica estatistica = Estatistica.getEstatistica(list);
        log.debug("Estatísticas calculadas com sucesso para {} transações", list.size());

        return EstatisticaOutput.toOutput(estatistica);
    }
}
